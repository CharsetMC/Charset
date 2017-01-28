package pl.asie.charset.pipes.shifter;

import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.FluidHandlerHelper;
import pl.asie.charset.pipes.pipe.PipeFluidContainer;
import pl.asie.charset.pipes.pipe.TilePipe;

/**
 * Created by asie on 1/28/17.
 */
public class ShifterExtractionHandlerFluids implements TileShifter.ExtractionHandler<IFluidHandler> {
	@Override
	public Capability<IFluidHandler> getCapability() {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public TileShifter.ExtractionType getExtractionType() {
		return TileShifter.ExtractionType.FLUIDS;
	}

	@Override
	public EnumActionResult extract(IFluidHandler inTank, TilePipe output, TileShifter shifter, EnumFacing direction) {
		IFluidHandler outTank = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, output, direction.getOpposite());
		if (outTank != null) {
			FluidStack stack = inTank.drain(PipeFluidContainer.TANK_RATE, false);
			if (stack != null && shifter.matches(stack)) {
				if (FluidHandlerHelper.push(inTank, outTank, stack) > 0) {
					return EnumActionResult.SUCCESS;
				}
			}
		}

		return EnumActionResult.FAIL;
	}
}
