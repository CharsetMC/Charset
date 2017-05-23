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

package pl.asie.charset.module.misc.drama;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "misc.drama",
		description = "Portable, official <Drama Generator> in Minecraft",
		isDefault = false
)
public class CharsetMiscDrama {
	@CharsetModule.Instance
	public static CharsetMiscDrama instance;

	public static Item dramaInABottle;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		dramaInABottle = new ItemDramaInABottle();

		RegistryUtils.register(dramaInABottle, "dramaInABottle");
		RegistryUtils.registerModel(dramaInABottle, 0, "charset:dramaInABottle");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		BrewingRecipeRegistry.addRecipe(new ItemStack(Items.POTIONITEM, 1, 16), "dyePink", new ItemStack(dramaInABottle));
	}
}
