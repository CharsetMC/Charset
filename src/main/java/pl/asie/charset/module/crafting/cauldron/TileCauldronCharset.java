/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.crafting.cauldron;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.module.crafting.cauldron.api.ICauldron;

import javax.annotation.Nullable;
import java.util.Collection;

public class TileCauldronCharset extends TileBase implements ICauldron, IFluidHandler, IFluidTankProperties {
	private FluidStack stack;

	public boolean isEmptyOrWater() {
		return stack == null || stack.getFluid() == FluidRegistry.WATER;
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);

		stack = FluidStack.loadFluidStackFromNBT(compound);

		if (isClient) {
			rebuildFromStack(false);
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);

		if (stack != null) {
			stack.writeToNBT(compound);
		}

		return compound;
	}

	public void rebuildFromStack(boolean emitUpdate) {
		markDirty();

		IBlockState currState = world.getBlockState(pos);
		IBlockState state = CharsetCraftingCauldron.blockCauldron.getDefaultState()
				.withProperty(BlockCauldron.LEVEL, isEmptyOrWater() ? getVanillaLevelValue() : 0);
		if (currState != state) {
			world.setBlockState(pos, state, 0);
			if (!world.isRemote && emitUpdate) {
				markBlockForUpdate();
			}
		}
	}

	private int getVanillaLevelValue() {
		if (CharsetCraftingCauldron.waterBottleSize == 0) {
			return getComparatorValue(15);
		} else {
			int maxBottles = 1000 / CharsetCraftingCauldron.waterBottleSize;
			int bottles = stack != null ? (stack.amount / CharsetCraftingCauldron.waterBottleSize) : 0;
			if (bottles <= 2) {
				return bottles;
			} else if (bottles == maxBottles) {
				return 3;
			} else {
				return 2;
			}
		}
	}

	private int levelToAmount(int level) {
		if (CharsetCraftingCauldron.waterBottleSize != 0) {
			return level * CharsetCraftingCauldron.waterBottleSize;
		}

		switch (level) {
			case 0:
			default:
				return 0;
			case 1:
				return 334;
			case 2:
				return 667;
			case 3:
				return 1000;
		}
	}

	public void rebuildFromVanillaLevel(IBlockState state) {
		if (isEmptyOrWater()) {
			FluidStack oldStack = stack;
			int oldLevel = getVanillaLevelValue();
			int newLevel = state.getValue(BlockCauldron.LEVEL);

			int oldAmount = oldStack != null ? oldStack.amount : 0;
			int oldLAmount = levelToAmount(oldLevel);
			int newLAmount = levelToAmount(newLevel);
			int newAmount = oldAmount + newLAmount - oldLAmount;

			if (oldLevel == 0 && newLevel == 3) {
				newAmount = getCapacity();
			} else if (oldLevel == 3 && newLevel == 0) {
				newAmount = 0;
			}

			if (newAmount == oldAmount) {
				return;
			}

			if (newAmount <= 0) stack = null;
			else if (newAmount > getCapacity()) stack = new FluidStack(FluidRegistry.WATER, getCapacity());
			else stack = new FluidStack(FluidRegistry.WATER, newAmount);

			int currLevel = newLevel;
			int desiredLevel = getVanillaLevelValue();
			if (currLevel != desiredLevel) {
				world.setBlockState(pos, state.withProperty(BlockCauldron.LEVEL, desiredLevel), 3);
				world.updateComparatorOutputLevel(pos, CharsetCraftingCauldron.blockCauldron);
			}
		}
	}

	@Override
	public int getComparatorValue(int max) {
		if (CharsetCraftingCauldron.waterBottleSize != 0) {
			int v = stack == null ? 0 : ((stack.amount + (CharsetCraftingCauldron.waterBottleSize - 1)) / CharsetCraftingCauldron.waterBottleSize);
			return MathHelper.clamp(v, 0, max);
		}

		if (stack == null) {
			return 0;
		} else if (stack.amount <= 333) {
			return 0;
		} else if (stack.amount <= 333 * 2) {
			return 1;
		} else if (stack.amount <= 333 * 3) {
			return 2;
		} else {
			return 3;
		}
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	public FluidStack getContents() {
		return stack;
	}

	protected void setContents(FluidStack stack) {
		int oldC = getComparatorValue(15);

		if (stack != null && stack.amount <= 0) {
			stack = null;
		}

		this.stack = stack;
		if (this.stack != null && this.stack.amount > getCapacity()) {
			ModCharset.logger.warn("Something tried to overflow cauldron @ " + getCauldronPos() + " with " + this.stack.amount + " mB of " + this.stack.getLocalizedName() + "!");
			this.stack = stack.copy();
			this.stack.amount = getCapacity();
		}

		rebuildFromStack(false);
		markBlockForUpdate();

		if (oldC != getComparatorValue(15)) {
			world.updateComparatorOutputLevel(pos, CharsetCraftingCauldron.blockCauldron);
		}
	}

	@Override
	public int getCapacity() {
		return 1000;
	}

	@Override
	public boolean canFill() {
		return true;
	}

	@Override
	public boolean canDrain() {
		return true;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluidStack) {
		return !fluidStack.getFluid().isGaseous(fluidStack);
	}

	@Override
	public boolean canDrainFluidType(FluidStack fluidStack) {
		return true;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] { this };
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (resource == null || !canFillFluidType(resource)) {
			return 0;
		}

		if (stack != null && !resource.isFluidEqual(stack)) {
			boolean isOkay = false;

			if (resource.getFluid() == FluidRegistry.WATER && stack.getFluid() == CharsetCraftingCauldron.dyedWater) {
				isOkay = true;
			}

			if (!isOkay) {
				return 0;
			}
		}

		int toFill = Math.min(stack != null ? (getCapacity() - stack.amount) : getCapacity(), resource.amount);
		if (doFill) {
			int oldC = getComparatorValue(15);

			if (stack == null) {
				stack = new FluidStack(resource, toFill);
			} else {
				stack.amount += toFill;
			}

			rebuildFromStack(false);
			markBlockForUpdate();

			if (oldC != getComparatorValue(15)) {
				world.updateComparatorOutputLevel(pos, CharsetCraftingCauldron.blockCauldron);
			}
		}

		return toFill;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (resource == null || stack == null || !resource.isFluidEqual(stack)) {
			return null;
		}

		return drain(resource.amount, doDrain);
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (stack == null) {
			return null;
		}

		FluidStack drainedStack = new FluidStack(stack, Math.min(stack.amount, maxDrain));
		if (drainedStack.amount == 0) {
			return null;
		}

		if (doDrain) {
			int oldC = getComparatorValue(15);

			if (drainedStack.amount == stack.amount) {
				stack = null;
			} else {
				stack.amount -= drainedStack.amount;
			}
			rebuildFromStack(false);
			markBlockForUpdate();

			if (oldC != getComparatorValue(15)) {
				world.updateComparatorOutputLevel(pos, CharsetCraftingCauldron.blockCauldron);
			}
		}

		return drainedStack;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		} else {
			return super.hasCapability(capability, facing);
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public float getFluidHeight() {
		return 6.0f + (stack != null ? (stack.amount * 9.0f / 1000) : 0);
	}

	@Override
	public World getCauldronWorld() {
		return getWorld();
	}

	@Override
	public BlockPos getCauldronPos() {
		return getPos();
	}

	@Override
	public Collection<EntityItem> getCauldronItemEntities(boolean immersed) {
		AxisAlignedBB axisAlignedBB = BlockCauldronCharset.AABB_INSIDE;

		if (immersed) {
			axisAlignedBB = new AxisAlignedBB(
					axisAlignedBB.minX,
					axisAlignedBB.minY,
					axisAlignedBB.minZ,
					axisAlignedBB.maxX,
					getFluidHeight(),
					axisAlignedBB.maxZ
			);
		}

		return world.getEntitiesWithinAABB(EntityItem.class, axisAlignedBB.offset(pos));
	}
}
