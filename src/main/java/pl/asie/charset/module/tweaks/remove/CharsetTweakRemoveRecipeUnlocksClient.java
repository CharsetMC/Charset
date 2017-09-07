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

package pl.asie.charset.module.tweaks.remove;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.Deque;
import java.util.List;

public class CharsetTweakRemoveRecipeUnlocksClient {
	private final MethodHandle TOASTS_QUEUE = MethodHandleHelper.findFieldGetter(GuiToast.class, "toastsQueue", "field_191792_h");
	private final MethodHandle RECIPES_OUTPUTS = MethodHandleHelper.findFieldGetter(RecipeToast.class, "recipesOutputs", "field_193666_c");

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderTickPre(TickEvent.RenderTickEvent event) {
		try {
			Deque deque = (Deque) TOASTS_QUEUE.invoke(Minecraft.getMinecraft().getToastGui());
			for (Object o : deque) {
				if (o instanceof RecipeToast) {
					((List) RECIPES_OUTPUTS.invoke(o)).clear();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
