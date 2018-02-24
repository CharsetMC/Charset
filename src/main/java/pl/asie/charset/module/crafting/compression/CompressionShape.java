/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.crafting.compression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.material.FastRecipeLookup;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.storage.barrels.BarrelUpgrade;
import pl.asie.charset.module.storage.barrels.BlockBarrel;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nullable;
import java.util.*;

public class CompressionShape {
	private static final int MAX_WIDTH = 3;
	private static final int MAX_HEIGHT = 3;

	protected final World world;
	protected final Multimap<EnumFacing, BlockPos> expectedFacings = MultimapBuilder.enumKeys(EnumFacing.class).arrayListValues().build();
	protected final List<TileCompressionCrafter> compressionCrafters = new ArrayList<>();
	protected final List<TileEntityDayBarrel> barrels = new ArrayList<>();
	protected int width, height;
	protected BlockPos topLeft;
	protected EnumFacing topDir, leftDir, bottomDir, rightDir;
	protected Orientation barrelOrientation;

	private boolean invalid = false;
	private final boolean[] lastRedstoneLevels = new boolean[6];

	protected long craftingTickStart = -1;
	protected long craftingTickEnd = -1;
	protected Set<EnumFacing> craftingDirections;
	protected BlockPos craftingSourcePos;
	protected EnumFacing craftingSourceDir;

	private CompressionShape(World world) {
		this.world = world;
	}

	protected float getRenderProgress(float partialTicks) {
		if (craftingTickStart < 0 || craftingTickEnd < 0) {
			return -1;
		}

		double duration = (craftingTickEnd - craftingTickStart) / 2f;
		double currTime = (world.getTotalWorldTime() - craftingTickStart) + partialTicks;
		if (currTime >= duration * 2) {
			return -1;
		} else if (currTime <= 0) {
			return 0;
		} else if (currTime >= duration) {
			currTime = (duration * 2) - currTime;
		}

		float progress = (float) Math.sin((float) (currTime * Math.PI / 2f / duration));
		return progress * (((width + height) / 2f) + 1) * 0.06f;
	}

	protected Set<EnumFacing> checkRedstoneLevels(boolean clearOnly) {
		EnumSet<EnumFacing> set = EnumSet.noneOf(EnumFacing.class);

		for (EnumFacing facing : EnumFacing.VALUES) {
			boolean lastLevel = lastRedstoneLevels[facing.ordinal()];
			boolean currLevel = getRedstoneLevel(facing);
			if (currLevel && !lastLevel) {
				set.add(facing);
			}
			if (!clearOnly || !currLevel) {
				lastRedstoneLevels[facing.ordinal()] = currLevel;
			}
		}

		return set;
	}

	private boolean getRedstoneLevel(EnumFacing side) {
		for (TileCompressionCrafter tile : compressionCrafters) {
			if (expectedFacings.get(side).contains(tile.getPos())) {
				if (tile.redstoneLevel) {
					return true;
				}
			}
		}

		return false;
	}

	public Orientation getBarrelOrientation() {
		return barrelOrientation;
	}

	public boolean isInvalid() {
		if (invalid) {
			return true;
		}

		for (Map.Entry<EnumFacing, BlockPos> entry : expectedFacings.entries()) {
			if (getCrafterDirection(entry.getValue()) != entry.getKey()) {
				invalid = true;
				return true;
			}
		}

		for (TileEntityDayBarrel barrel : barrels) {
			if (barrel.isInvalid() || barrel.orientation != barrelOrientation) {
				invalid = true;
				return true;
			}
		}

		return false;
	}

	private void addItemHandlers(Collection<IItemInsertionHandler> outputs, EnumFacing sourceDir, Collection<BlockPos> positions) {
		for (BlockPos sourcePos : positions) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (facing == sourceDir) continue;

				BlockPos spos = sourcePos.offset(facing);
				if (!positions.contains(spos)) {
					IItemInsertionHandler output = CapabilityHelper.get(
							world, spos, Capabilities.ITEM_INSERTION_HANDLER, facing.getOpposite(),
							false, true, false
					);
					if (output != null) {
						outputs.add(output);
					}
				}
			}
		}
	}

	private boolean outputStack(ItemStack stack, BlockPos sourcePos, EnumFacing sourceDir, Collection<IItemInsertionHandler> outputs, boolean simulate) {
		for (IItemInsertionHandler output : outputs) {
			if (stack.isEmpty()) break;
			stack = output.insertItem(stack, simulate);
		}

		if (!stack.isEmpty()) {
			if (!simulate) {
				ItemUtils.spawnItemEntity(
						world, new Vec3d(sourcePos.offset(sourceDir.getOpposite())).addVector(0.5, 0.5, 0.5), stack, 0, 0, 0, 0
				);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public boolean craftBegin(TileCompressionCrafter sender, EnumFacing sourceDir) {
		if (craftingTickEnd >= world.getTotalWorldTime()) {
			return false;
		}

		Set<EnumFacing> validSides = checkRedstoneLevels(false);
		if (validSides.isEmpty()) {
			return false;
		}

		craftingDirections = validSides;
		craftingSourcePos = sender.getPos();
		craftingSourceDir = sourceDir;
		Optional<String> error = craftEnd(true);
		if (!error.isPresent()) {
			craftingTickStart = world.getTotalWorldTime();
			craftingTickEnd = world.getTotalWorldTime() + 20;
			CharsetCraftingCompression.proxy.markShapeRender(sender, this);
			return true;
		} else {
			if (error.get().length() > 0) {
				new Notice(sender, new TextComponentTranslation(error.get())).sendToAll();
			}
			return false;
		}
	}

	public Optional<String> craftEnd(boolean simulate) {
		InventoryCrafting crafting = RecipeUtils.getCraftingInventory(width, height);
		boolean hasNonEmpty = false;

		for (int i = 0; i < width * height; i++) {
			ItemStack stack = barrels.get(i).item;
			if (!stack.isEmpty()) {
				hasNonEmpty = true;
				boolean copied = false;
				if (barrels.get(i).upgrades.contains(BarrelUpgrade.STICKY)) {
					stack = stack.copy();
					stack.shrink(1);
					copied = true;
				}
				if (stack.getCount() > 1) {
					if (!copied) {
						stack = stack.copy();
						copied = true;
					}
					stack.setCount(1);
				}
			}
			crafting.setInventorySlotContents(i, stack);
		}

		if (!hasNonEmpty) {
			return Optional.of("");
		}

		IRecipe recipe = FastRecipeLookup.findMatchingRecipe(crafting, world);
		if (recipe == null) {
			return Optional.of("notice.charset.compression.cannot_craft");
		}
		ItemStack stack = recipe.getCraftingResult(crafting);
		if (stack.isEmpty()) {
			return Optional.of("notice.charset.compression.cannot_craft");
		}

		Set<EnumFacing> validSides = craftingDirections;
		BlockPos sourcePos = craftingSourcePos;
		EnumFacing sourceDir = craftingSourceDir;

		List<IItemInsertionHandler> outputs = new ArrayList<>();
		for (EnumFacing facing : validSides) {
			addItemHandlers(outputs, facing, expectedFacings.get(facing));
		}

		if (outputs.isEmpty()) {
			return Optional.of("notice.charset.compression.need_output");
		}

		if (!outputStack(stack.copy(), sourcePos, sourceDir, outputs, simulate)) {
			return Optional.of("notice.charset.compression.need_output_room");
		}

		NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(crafting);
		for (int i = 0; i < width * height; i++) {
			ItemStack source = barrels.get(i).item;
			ItemStack target = remainingItems.get(i);

			ItemStack sourceOrig = source;
			if (!source.isEmpty() && !barrels.get(i).upgrades.contains(BarrelUpgrade.INFINITE)) {
				sourceOrig = source.copy();
				if (!simulate) {
					source.shrink(1);
				}
			}

			if (target.isEmpty()) {
				// we're fine
			} else if (ItemUtils.canMerge(sourceOrig, target)) {
				if (!simulate) {
					source.grow(target.getCount());
				}
			} else {
				if (!outputStack(target, sourcePos, sourceDir, outputs, simulate)) {
					if (simulate) {
						return Optional.of("notice.charset.compression.need_output_room");
					}
				}
			}

			if (!simulate) {
				barrels.get(i).setItem(source);
			}
		}

		return Optional.empty();
	}

	private boolean setCrafterShapeIfMatchesDirection(BlockPos pos, EnumFacing facing) {
		if (!world.isBlockLoaded(pos, true)) return false;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockCompressionCrafter && state.getValue(Properties.FACING) == facing) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileCompressionCrafter) {
				((TileCompressionCrafter) tile).shape = this;
				compressionCrafters.add((TileCompressionCrafter) tile);
				expectedFacings.put(facing, pos);
				return true;
			}
		}

		return false;
	}

	@Nullable
	private EnumFacing getCrafterDirection(BlockPos pos) {
		if (!world.isBlockLoaded(pos, true)) return null;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockCompressionCrafter) {
			return state.getValue(Properties.FACING);
		} else {
			return null;
		}
	}

	private TileEntityDayBarrel getBarrel(BlockPos pos) {
		if (!world.isBlockLoaded(pos, true)) return null;

		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileEntityDayBarrel ? (TileEntityDayBarrel) tile : null;
	}

	public static CompressionShape build(World world, BlockPos start) {
		CompressionShape shape = new CompressionShape(world);
		EnumFacing firstBarrelFacing = shape.getCrafterDirection(start);
		if (firstBarrelFacing == null) {
			return null;
		}

		BlockPos firstBarrelPos = start.offset(firstBarrelFacing);
		TileEntity tile = world.getTileEntity(firstBarrelPos);
		if (tile instanceof TileEntityDayBarrel) {
			shape.barrelOrientation = ((TileEntityDayBarrel) tile).orientation;
		} else {
			return null;
		}

		// first, find the topleftmost barrel
		BlockPos topLeft = firstBarrelPos;
		EnumFacing topDir = shape.barrelOrientation.top;

		// go up
		while (shape.getBarrel(topLeft.offset(topDir)) != null) {
			topLeft = topLeft.offset(topDir);
		}

		// go to the left
		EnumFacing leftDir = shape.barrelOrientation.getPrevRotationOnFace().top;
		while (shape.getBarrel(topLeft.offset(leftDir)) != null) {
			topLeft = topLeft.offset(leftDir);
		}

		// now calculate the width and the height
		int width = 1;
		BlockPos tmp = topLeft;
		while (shape.getBarrel(tmp.offset(leftDir.getOpposite())) != null) {
			tmp = tmp.offset(leftDir.getOpposite());
			width++;
			if (width > MAX_WIDTH) {
				return null;
			}
		}

		int height = 1;
		while (true) {
			tmp = topLeft.offset(topDir.getOpposite(), height);
			int wDetected = 0;
			while (shape.getBarrel(tmp) != null) {
				tmp = tmp.offset(leftDir.getOpposite());
				wDetected++;
			}
			if (wDetected == 0) {
				// last line
				break;
			} else if (wDetected != width) {
				// width disparity!
				return null;
			} else {
				height++;
				if (height > MAX_HEIGHT) {
					return null;
				}
			}
		}

		// now create an array and add all the barrels in
		shape.width = width;
		shape.height = height;

		shape.topDir = topDir;
		shape.leftDir = leftDir;
		shape.bottomDir = topDir.getOpposite();
		shape.rightDir = leftDir.getOpposite();
		shape.topLeft = topLeft;

		for (int yPos = 0; yPos < height; yPos++) {
			tmp = topLeft.offset(shape.bottomDir, yPos);
			for (int xPos = 0; xPos < width; xPos++) {
				// add barrel
				TileEntityDayBarrel barrel = shape.getBarrel(tmp);
				if (barrel == null) {
					ModCharset.logger.warn("Should never happen!", new Throwable());
					return null;
				}
				shape.barrels.add(barrel);

				// check sides
				if (xPos == 0 && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(leftDir), shape.rightDir)) {
					return null;
				} else if (xPos == (width - 1) && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(shape.rightDir), leftDir)) {
					return null;
				}

				if (yPos == 0 && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(topDir), shape.bottomDir)) {
					return null;
				} else if (yPos == (height - 1) && !shape.setCrafterShapeIfMatchesDirection(tmp.offset(shape.bottomDir), topDir)) {
					return null;
				}

				tmp = tmp.offset(shape.rightDir);
			}
		}

		return shape;
	}

	public void tick() {
		if (craftingTickStart >= 0) {
			if (isInvalid()) {
				craftingTickStart = craftingTickEnd = -1;
			}

			long time = world.getTotalWorldTime();
			if (time >= craftingTickStart && time < craftingTickEnd) {
				checkRedstoneLevels(true);
			} else if (time >= craftingTickEnd) {
				craftingTickStart = craftingTickEnd = -1;
				if (!world.isRemote) {
					craftEnd(false);
				}
			}
		}
	}
}
