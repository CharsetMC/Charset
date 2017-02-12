/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.pipes.shifter;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.TreeMultimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.FluidHandlerHelper;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RedstoneUtils;
import pl.asie.charset.pipes.CharsetPipes;
import pl.asie.charset.pipes.PipeUtils;
import pl.asie.charset.pipes.pipe.TilePipe;

import javax.annotation.Nullable;
import java.util.Comparator;

public class TileShifter extends TileBase implements IShifter, ITickable {
	public enum ExtractionType {
		ITEMS(16),
		FLUIDS(1);

		public final int tickerSpeed;

		ExtractionType(int tickerSpeed) {
			this.tickerSpeed = tickerSpeed;
		}
	}

	public interface ExtractionHandler<T> {
		Capability<T> getCapability();

		default int getPriority() {
			return 0;
		}

		ExtractionType getExtractionType();
		EnumActionResult extract(T input, TilePipe output, TileShifter shifter, EnumFacing direction);
	}

	private static final Multimap<ExtractionType, ExtractionHandler<?>> extractionHandlers =
			MultimapBuilder.enumKeys(ExtractionType.class)
			.treeSetValues((a, b) -> {
				int p = ((ExtractionHandler) b).getPriority() - ((ExtractionHandler) a).getPriority();
				if (p != 0) return p * 2;
				else if (b == a) return 0;
				else return b.hashCode() > a.hashCode() ? 1 : -1;
			}).build();

	public static void registerExtractionHandler(ExtractionHandler<?> handler) {
		extractionHandlers.put(handler.getExtractionType(), handler);
	}

	public static void registerDefaultHandlers() {
		registerExtractionHandler(new ShifterExtractionHandlerItems());
		registerExtractionHandler(new ShifterExtractionHandlerFluids());
	}

	private final ItemStack[] filters = new ItemStack[6];
	private boolean updateRedstoneLevels;
	private int redstoneLevel;
	private int ticker = CharsetPipes.instance.rand.nextInt(256);

	// CommonCapabilities values
	public int ccInvHash;
	public boolean ccInvHashSet;

	public TileShifter() {
		for (int i = 0; i < filters.length; i++)
			filters[i] = ItemStack.EMPTY;
	}

	public EnumFacing getDirection(IBlockState state) {
		return state.getValue(Properties.FACING);
	}

	public EnumFacing getDirection() {
		if (world != null) {
			return getDirection(world.getBlockState(pos));
		} else {
			return EnumFacing.UP;
		}
	}

	private boolean isInput(TileEntity input, EnumFacing direction) {
		if (input != null) {
			if (PipeUtils.getPipe(input) != null) {
				return false;
			}

			for (ExtractionHandler handler : extractionHandlers.values()) {
				if (CapabilityHelper.get(handler.getCapability(), input, direction) != null) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Mode getMode() {
		EnumFacing direction = getDirection();
		TileEntity input = getNeighbourTile(direction.getOpposite());

		return isInput(input, direction) ? Mode.Extract : Mode.Shift;
	}

	public ItemStack[] getFilters() {
		return filters;
	}

	public void setFilter(int side, ItemStack stack) {
		filters[side] = stack;
		markBlockForUpdate();
	}

	public int getRedstoneLevel() {
		return redstoneLevel;
	}

	@Override
	public void validate() {
		super.validate();
		updateRedstoneLevels = true;
	}

	public int getShiftDistance() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isShifting() {
		return getRedstoneLevel() > 0;
	}

	@Override
	public boolean hasFilter() {
		for (ItemStack s : filters) {
			if (!s.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean matches(ItemStack source) {
		int filterCount = 0;
		boolean matches = false;
		for (int i = 0; i < 6; i++) {
			if (!filters[i].isEmpty()) {
				filterCount++;
				if (ItemUtils.equals(source, filters[i], false, filters[i].getHasSubtypes(), false)) {
					matches = true;
					break;
				}
			}
		}

		return filterCount == 0 || matches;
	}

	@Override
	public boolean matches(FluidStack stack) {
		if (stack == null) {
			return false;
		}

		if (!hasFilter()) {
			return true;
		}

		for (ItemStack s : filters) {
			if (!s.isEmpty()) {
				IFluidHandler handler = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, s, null);
				if (handler != null && FluidHandlerHelper.matches(handler, stack)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void update() {
		super.update();

		if (world.isRemote) {
			return;
		}

		if (updateRedstoneLevels) {
			updateRedstoneLevel(null);
			updateRedstoneLevels = false;
		}

		ticker++;

		if (redstoneLevel > 0) {
			EnumFacing direction = getDirection();
			TileEntity input = getNeighbourTile(direction.getOpposite());
			TilePipe output = PipeUtils.getPipe(getWorld(), getPos().offset(direction), direction.getOpposite());

			for (ExtractionType type : extractionHandlers.keySet()) {
				if (ticker % type.tickerSpeed == 0) {
					EnumActionResult result = EnumActionResult.PASS;

					for (ExtractionHandler handler : extractionHandlers.get(type)) {
						Object inputHandler = CapabilityHelper.get(handler.getCapability(), input, direction);
						if (inputHandler != null) {
							result = handler.extract(inputHandler, output, this, direction);
							if (result != EnumActionResult.PASS)
								break;
						}
					}
				}
			}
		}
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		redstoneLevel = nbt.getByte("rs");

		NBTTagList filterList = nbt.getTagList("filters", 10);
		for (int i = 0; i < Math.min(filterList.tagCount(), filters.length); i++) {
			NBTTagCompound cpd = filterList.getCompoundTagAt(i);
			filters[i] = new ItemStack(cpd);
		}

		if (isClient) {
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		nbt.setByte("rs", (byte) redstoneLevel);

		NBTTagList filterList = new NBTTagList();
		for (int i = 0; i < filters.length; i++) {
			NBTTagCompound fnbt = new NBTTagCompound();
			filters[i].writeToNBT(fnbt);
			filterList.appendTag(fnbt);
		}
		nbt.setTag("filters", filterList);

		return nbt;
	}

	public void updateRedstoneLevel(@Nullable BlockPos neighborPos) {
		int oldRedstoneLevel = redstoneLevel;

		redstoneLevel = 0;
		for (EnumFacing d : EnumFacing.VALUES) {
			redstoneLevel = Math.max(redstoneLevel, RedstoneUtils.getRedstonePower(world, pos.offset(d), d));
		}

		if (oldRedstoneLevel != redstoneLevel) {
			markBlockForUpdate();
		}

		EnumFacing direction = getDirection();
		TilePipe output = PipeUtils.getPipe(getWorld(), getPos().offset(direction), direction.getOpposite());
		if (output != null) {
			if ((getMode() == Mode.Extract && !output.connects(direction.getOpposite()))
					|| (getMode() == Mode.Shift && output.connects(direction.getOpposite()))) {
				world.neighborChanged(pos.offset(getDirection()), getBlockType(), pos);
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return ((facing == null || facing == getDirection()) && capability == CharsetPipes.CAP_SHIFTER)
				|| super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if ((facing == null || facing == getDirection()) && capability == CharsetPipes.CAP_SHIFTER) {
			return (T) this;
		}
		return super.getCapability(capability, facing);
	}
}
