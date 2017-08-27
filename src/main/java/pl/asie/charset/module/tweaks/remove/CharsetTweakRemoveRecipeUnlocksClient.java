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
