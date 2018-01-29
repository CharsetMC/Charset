package pl.asie.charset.module.tweak.improvedCauldron;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import pl.asie.charset.lib.block.TileBase;

import javax.annotation.Nullable;

public class TileCauldronCharset extends TileBase implements IFluidHandler, IFluidTankProperties {
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
			markBlockForUpdate();
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
		IBlockState currState = world.getBlockState(pos);
		IBlockState state = CharsetTweakImprovedCauldron.blockCauldron.getDefaultState()
				.withProperty(BlockCauldron.LEVEL, isEmptyOrWater() ? getComparatorValue() : 0);
		if (currState != state) {
			world.setBlockState(pos, state, 0);
			if (!world.isRemote && emitUpdate) {
				markBlockForUpdate();
			}
		}
	}

	private int levelToAmount(int level) {
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
			int oldAmount = oldStack != null ? oldStack.amount : 0;
			int oldLAmount = levelToAmount(getComparatorValue());
			int newLAmount = levelToAmount(state.getValue(BlockCauldron.LEVEL));
			int newAmount = oldAmount + newLAmount - oldLAmount;

			if (newAmount == oldAmount) {
				return;
			}

			if (newAmount <= 0) stack = null;
			else if (newAmount > getCapacity()) stack = new FluidStack(FluidRegistry.WATER, getCapacity());
			else stack = new FluidStack(FluidRegistry.WATER, newAmount);

			if (oldStack != stack && (stack == null || oldStack == null || !stack.isFluidEqual(oldStack))) {
				world.updateComparatorOutputLevel(pos, CharsetTweakImprovedCauldron.blockCauldron);
				markBlockForUpdate();
			}
		}
	}

	@Override
	public int getComparatorValue() {
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
		int oldC = getComparatorValue();

		if (stack != null && stack.amount <= 0) {
			stack = null;
		}

		this.stack = stack;
		rebuildFromStack(false);
		markBlockForUpdate();

		if (oldC != getComparatorValue()) {
			world.updateComparatorOutputLevel(pos, CharsetTweakImprovedCauldron.blockCauldron);
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
		return true;
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
		if (resource == null) {
			return 0;
		}

		if (stack != null && !resource.isFluidEqual(stack)) {
			boolean isOkay = false;

			if (resource.getFluid() == FluidRegistry.WATER && stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
				isOkay = true;
			}

			if (!isOkay) {
				return 0;
			}
		}

		int toFill = Math.min(stack != null ? (getCapacity() - stack.amount) : getCapacity(), resource.amount);
		if (doFill) {
			int oldC = getComparatorValue();

			if (stack == null) {
				stack = new FluidStack(resource, toFill);
			} else {
				stack.amount += toFill;
			}

			rebuildFromStack(false);
			markBlockForUpdate();

			if (oldC != getComparatorValue()) {
				world.updateComparatorOutputLevel(pos, CharsetTweakImprovedCauldron.blockCauldron);
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
			int oldC = getComparatorValue();

			if (drainedStack.amount == stack.amount) {
				stack = null;
			} else {
				stack.amount -= drainedStack.amount;
			}
			rebuildFromStack(false);
			markBlockForUpdate();

			if (oldC != getComparatorValue()) {
				world.updateComparatorOutputLevel(pos, CharsetTweakImprovedCauldron.blockCauldron);
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
}
