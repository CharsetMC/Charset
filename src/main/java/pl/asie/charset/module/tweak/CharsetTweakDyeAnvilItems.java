/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tweak;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ColorUtils;

@CharsetModule(
		name = "tweak.anvilDyeItems",
		description = "Allows dyeing item names in an anvil",
		antidependencies = "mod:quark",
		profile = ModuleProfile.STABLE
)
// TODO: Override GuiIngame.highlightingItemStack?
public class CharsetTweakDyeAnvilItems {
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		if (!event.getLeft().isEmpty() && !event.getRight().isEmpty()
				&& event.getLeft().getCount() == event.getRight().getCount()) {
			EnumDyeColor color = ColorUtils.getDyeColor(event.getRight());
			if (color != null) {
				event.setCost(event.getLeft().getCount() * 3);

				if (event.getOutput().isEmpty()) {
					event.setOutput(event.getLeft().copy());
				}

				NBTTagCompound compound = event.getOutput().getOrCreateSubCompound("display");
				compound.setByte("_chdytw_color", (byte) color.getDyeDamage());
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public void onItemTooltip(ItemTooltipEvent event) {
		NBTTagCompound compound = event.getItemStack().getTagCompound();
		if (compound != null && compound.hasKey("display", Constants.NBT.TAG_COMPOUND)) {
			NBTTagCompound displayCpd = compound.getCompoundTag("display");
			if (displayCpd.hasKey("_chdytw_color")) {
				EnumDyeColor color = EnumDyeColor.byDyeDamage(displayCpd.getByte("_chdytw_color"));

				String s = event.getItemStack().getDisplayName();
				for (int i = 0; i < event.getToolTip().size(); i++) {
					String s2 = TextFormatting.getTextWithoutFormattingCodes(event.getToolTip().get(i));
					if (s.equals(s2)) {
						event.getToolTip().set(i, ColorUtils.getNearestTextFormatting(color) + s2);
						return;
					}
				}
			}
		}
	}
}
