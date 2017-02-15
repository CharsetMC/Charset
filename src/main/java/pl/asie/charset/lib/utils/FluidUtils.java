package pl.asie.charset.lib.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidHandlerWrapper;

import javax.annotation.Nullable;

public final class FluidUtils {
    private FluidUtils() {

    }

    public static IFluidHandler getFluidHandler(TileEntity tile, EnumFacing dir) {
        if (tile != null) {
            if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir)) {
                return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir);
            } else if (tile instanceof net.minecraftforge.fluids.IFluidHandler) {
                return new FluidHandlerWrapper((net.minecraftforge.fluids.IFluidHandler) tile, dir);
            }
        }

        return null;
    }

    public static void push(IFluidHandler from, @Nullable IFluidHandler to, int amt) {
        if (amt > 0 && to != null) {
            FluidStack drained = from.drain(amt, false);
            if (drained != null && drained.amount > 0) {
                amt = to.fill(drained, true);
                if (amt > 0) {
                    FluidStack toDrain = drained.copy();
                    toDrain.amount = amt;
                    from.drain(toDrain, true);
                }
            }
        }
    }

    public static void push(IFluidHandler from, @Nullable IFluidHandler to, FluidStack out) {
        if (to != null && out != null && out.amount > 0) {
            FluidStack drained = from.drain(out, false);
            if (drained != null && drained.amount > 0) {
                int amt = to.fill(drained, true);
                if (amt > 0) {
                    FluidStack toDrain = drained;
                    toDrain.amount = amt;
                    from.drain(toDrain, true);
                }
            }
        }
    }
}
