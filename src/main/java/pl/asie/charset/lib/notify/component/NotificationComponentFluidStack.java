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

package pl.asie.charset.lib.notify.component;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.handlers.FluidExtraInformationHandler;
import pl.asie.charset.lib.utils.FluidUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;

public class NotificationComponentFluidStack extends NotificationComponent {
	@Nullable
	private final FluidStack stack;
	private final boolean showInfo;

	public NotificationComponentFluidStack(@Nullable FluidStack stack, boolean showInfo) {
		this.stack = stack;
		this.showInfo = showInfo;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof NotificationComponentFluidStack)) {
			return false;
		} else {
			NotificationComponentFluidStack ncs = (NotificationComponentFluidStack) other;
			if (this.showInfo != ncs.showInfo) {
				return false;
			}

			if (this.stack == ncs.stack) {
				return true;
			}

			if (this.stack != null && ncs.stack != null) {
				return this.stack.isFluidStackIdentical(ncs.stack);
			} else {
				// only one is null
				return false;
			}
		}
	}


	@Override
	public String toString() {
		if (stack != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(FluidUtils.getCorrectLocalizedName(stack));

			if (showInfo) {
				builder.append('\n');
				ArrayList<String> bits = new ArrayList<>();

				boolean oldAIT = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
				Minecraft.getMinecraft().gameSettings.advancedItemTooltips = false;
				try {
					FluidExtraInformationHandler.addInformation(stack, bits);
				} catch (Throwable t) {
					t.printStackTrace();
					bits.add("" + TextFormatting.RED + TextFormatting.BOLD + "ERROR");
				}
				Minecraft.getMinecraft().gameSettings.advancedItemTooltips = oldAIT;

				boolean tail = false;
				for (String s : bits) {
					if (tail) {
						builder.append('\n');
					} else {
						tail = true;
					}
					builder.append(s);
				}
			}

			return I18n.translateToLocalFormatted("notice.charset.fluid", Integer.toString(stack.amount), builder.toString());
		} else {
			return I18n.translateToLocal("notice.charset.fluid.empty");
		}
	}

	public static class Factory implements NotificationComponentFactory<NotificationComponentFluidStack> {
		@Override
		public Class<NotificationComponentFluidStack> getComponentClass() {
			return NotificationComponentFluidStack.class;
		}

		@Override
		public void serialize(NotificationComponentFluidStack component, PacketBuffer buffer) {
			buffer.writeBoolean(component.showInfo);
			buffer.writeBoolean(component.stack != null);
			if (component.stack != null) {
				NBTTagCompound cpd = new NBTTagCompound();
				component.stack.writeToNBT(cpd);
				buffer.writeCompoundTag(cpd);
			}
		}

		@Override
		public NotificationComponentFluidStack deserialize(PacketBuffer buffer) {
			boolean showInfo = buffer.readBoolean();
			if (buffer.readBoolean()) {
				try {
					NBTTagCompound compound = buffer.readCompoundTag();
					FluidStack stack = FluidStack.loadFluidStackFromNBT(compound);
					return new NotificationComponentFluidStack(stack, showInfo);
				} catch (IOException e) {
					// pass
				}
			}

			return new NotificationComponentFluidStack(null, showInfo);
		}
	}
}
