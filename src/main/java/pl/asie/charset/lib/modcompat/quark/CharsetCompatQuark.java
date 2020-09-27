/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.lib.modcompat.quark;

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
		name = "quark:lib",
		profile = ModuleProfile.COMPAT,
		dependencies = {"mod:quark"}
)
public class CharsetCompatQuark {
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ResourceLocation location = new ResourceLocation("quark:stained_planks");
		Item it = ForgeRegistries.ITEMS.getValue(location);
		ItemStack stick = new ItemStack(Items.STICK);
		ItemMaterial stickMaterial = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(stick);

		if (it instanceof ItemBlock) {
			for (int i = 0; i < 16; i++) {
				ItemStack wood = new ItemStack(it, 1, i);
				ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(wood);
				if (material != null) {
					return;
				}

				material = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(wood);
				ItemMaterialRegistry.INSTANCE.registerTypes(material, "wood", "plank", "block");
				ItemMaterialRegistry.INSTANCE.registerRelation(material, stickMaterial, "stick");
			}
		}
	}
}
