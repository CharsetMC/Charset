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

package pl.asie.charset.module.misc.drama;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tools.building.ToolItemColor;

@CharsetModule(
		name = "misc.drama",
		description = "Portable, official <Drama Generator> in Minecraft",
		profile = ModuleProfile.STABLE,
		isDefault = false
)
public class CharsetMiscDrama {
	@CharsetModule.Instance
	public static CharsetMiscDrama instance;

	public static Item dramaInABottle;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		dramaInABottle = new ItemDramaInABottle();
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), dramaInABottle, "dramaInABottle");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(dramaInABottle, 0, "charset:dramaInABottle");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler((stack, tintIndex) -> (tintIndex == 1 ? 0xFF98D0 : -1), dramaInABottle);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		BrewingRecipeRegistry.addRecipe(new ItemStack(Items.POTIONITEM, 1, 16), "dyePink", new ItemStack(dramaInABottle));
	}
}
