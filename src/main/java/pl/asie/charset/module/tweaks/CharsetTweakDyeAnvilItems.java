/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) 2014 copygirl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.module.tweaks;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ColorUtils;

@CharsetModule(
		name = "tweak.anvilDyeItems",
		description = "Allows dyeing item names in an anvil",
		antidependencies = "mod:quark",
		profile = ModuleProfile.UNSTABLE
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
