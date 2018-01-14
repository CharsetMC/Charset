package pl.asie.charset.module.storage.locks.wrapper;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class ReadOnlyFluidHandler implements IFluidHandler {
	private final IFluidHandler parent;

	public ReadOnlyFluidHandler(IFluidHandler parent) {
		this.parent = parent;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return parent.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}
}
