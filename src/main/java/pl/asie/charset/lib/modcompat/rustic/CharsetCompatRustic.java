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

package pl.asie.charset.lib.modcompat.rustic;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ColorUtils;

@CharsetModule(
		name = "rustic:lib",
		profile = ModuleProfile.COMPAT,
		dependencies = {"mod:rustic"}
)
public class CharsetCompatRustic {
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (EnumDyeColor color : EnumDyeColor.values()) {
			ResourceLocation location = new ResourceLocation(ColorUtils.getOreDictEntry("rustic:painted_wood_", color));
			Item i = ForgeRegistries.ITEMS.getValue(location);
			if (i instanceof ItemBlock) {
				ItemStack wood = new ItemStack(i);
				ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(wood);
				if (material != null) {
					continue;
				}

				OreDictionary.registerOre("plankWood", i);
				OreDictionary.registerOre("plankStained", i); // (Quark)

				material = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(wood);
				ItemMaterialRegistry.INSTANCE.registerTypes(material, "wood", "plank", "block");

				ItemStack stick = new ItemStack(Items.STICK);
				ItemMaterial stickMaterial = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(stick);
				ItemMaterialRegistry.INSTANCE.registerRelation(material, stickMaterial, "stick");
			}
		}
	}
}
