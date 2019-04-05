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

package pl.asie.charset.lib.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.lib.IFluidExtraInformation;
import pl.asie.charset.lib.utils.ThreeState;
import pl.asie.charset.lib.utils.UtilProxyCommon;

import javax.annotation.Nullable;
import java.util.List;

public class FluidExtraInformationHandler {
    @SideOnly(Side.CLIENT)
    public static void addInformation(FluidStack stack, List<String> tooltip, ITooltipFlag flag) {
        if (stack != null) {
            World world = Minecraft.getMinecraft().world;

            if (stack.getFluid() instanceof IFluidExtraInformation) {
                ((IFluidExtraInformation) stack.getFluid()).addInformation(stack, world, tooltip, flag);
            }

            if (flag == ITooltipFlag.TooltipFlags.ADVANCED) {
                tooltip.add("");
                tooltip.add(TextFormatting.DARK_GRAY + "Density: " + stack.getFluid().getDensity(stack) + " kg/m^3");
                tooltip.add(TextFormatting.DARK_GRAY + "Temperature: " + stack.getFluid().getTemperature(stack) + " K");
                tooltip.add(TextFormatting.DARK_GRAY + "Viscosity: " + stack.getFluid().getViscosity(stack) + " m/s^2 (x10^-3)");
            }
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
                IFluidTankProperties[] properties = handler.getTankProperties();
                if (properties != null && properties.length == 1 && properties[0] != null) {
                    FluidStack contents = properties[0].getContents();
                    if (contents != null) {
                        UtilProxyCommon.proxy.addInformation(contents, UtilProxyCommon.proxy.getLocalWorld(), event.getToolTip(), ThreeState.MAYBE);
                    }
                }
            }
        }
    }
}
