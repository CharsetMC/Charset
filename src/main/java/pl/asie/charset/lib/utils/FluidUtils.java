package pl.asie.charset.lib.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public final class FluidUtils {
    private FluidUtils() {

    }

    public static boolean matches(IFluidHandler handler, FluidStack stack) {
        IFluidTankProperties[] properties = handler.getTankProperties();
        if (properties != null) for (IFluidTankProperties property : properties) {
            FluidStack match = property.getContents();
            if (match.isFluidEqual(stack)) {
                return true;
            }
        }

        return false;
    }

    public static int push(IFluidHandler from, IFluidHandler to, int amt) {
        if (amt > 0) {
            FluidStack drained = from.drain(amt, false);
            if (drained != null && drained.amount > 0) {
                amt = to.fill(drained, true);
                if (amt > 0) {
                    FluidStack toDrain = drained.copy();
                    toDrain.amount = amt;
                    FluidStack drainedReal = from.drain(toDrain, true);
                    return drainedReal.amount;
                }
            }
        }

        return 0;
    }

    public static int push(IFluidHandler from, IFluidHandler to, FluidStack out) {
        if (out != null && out.amount > 0) {
            FluidStack drained = from.drain(out, false);
            if (drained != null && drained.amount > 0) {
                int amt = to.fill(drained, true);
                if (amt > 0) {
                    FluidStack toDrain = drained;
                    toDrain.amount = amt;
                    FluidStack drainedReal = from.drain(toDrain, true);
                    return drainedReal.amount;
                }
            }
        }

        return 0;
    }
}
