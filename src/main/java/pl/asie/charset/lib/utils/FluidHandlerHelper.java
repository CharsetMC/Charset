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

package pl.asie.charset.lib.utils;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public final class FluidHandlerHelper {
    private FluidHandlerHelper() {

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

    public static int push(IFluidHandler from, IFluidHandler to, FluidStack pushed) {
        if (pushed != null && pushed.amount > 0) {
            FluidStack drained = from.drain(pushed, false);
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
