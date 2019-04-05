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

package pl.asie.charset.module.tweak.remove;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.recipe.RecipeDummy;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@CharsetModule(
		name = "tweak.remove.vanillaStyleTools",
		profile = ModuleProfile.STABLE,
		isDefault = false
)
public class CharsetTweakRemoveVanillaTools {
	@CharsetModule.Configuration
	public static Configuration config;

	private boolean neutralize, disableRecipes;
	private Set<String> disabledClasses = new HashSet<>();

	@Mod.EventHandler
	public void loadConfig(CharsetLoadConfigEvent event) {
		neutralize = config.getBoolean("neutralizeItems", "features", true, "Set to true to enable neutralizing tool items (setting maximum damage to 1).");
		disableRecipes = config.getBoolean("disableRecipes", "features", true, "Set to true to enable disabling recipes for tool items.");

		for (String s : new String[] { "pickaxe", "axe", "hoe", "sword", "spade" }) {
			if (config.getBoolean(s, "disabledClasses", true, "")) {
				disabledClasses.add(s);
			}
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Set<Item> itemSet = new HashSet<Item>();
		for (ResourceLocation l : Item.REGISTRY.getKeys()) {
			Item i = Item.REGISTRY.getObject(l);
			if ((i instanceof ItemPickaxe && disabledClasses.contains("pickaxe"))
					|| (i instanceof ItemAxe && disabledClasses.contains("axe"))
					|| (i instanceof ItemHoe && disabledClasses.contains("hoe"))
					|| (i instanceof ItemSpade && disabledClasses.contains("spade"))
					|| (i instanceof ItemSword && disabledClasses.contains("sword"))
					) {
				if (neutralize) i.setMaxDamage(1);
				itemSet.add(i);
			}
		}

		if (disableRecipes) {
			// IForgeRegistryModifiable modRecipeRegistry = (IForgeRegistryModifiable) ForgeRegistries.RECIPES;

			Iterator<IRecipe> iterator = CraftingManager.REGISTRY.iterator();
			while (iterator.hasNext()) {
				IRecipe recipe = iterator.next();
				ItemStack output = recipe.getRecipeOutput();
				if (!output.isEmpty() && itemSet.contains(output.getItem())) {
					// modRecipeRegistry.remove(recipe.getRegistryName());
					ForgeRegistries.RECIPES.register(new RecipeDummy(recipe.getGroup()).setRegistryName(recipe.getRegistryName()));
					itemSet.remove(output.getItem());
					ModCharset.logger.info("Disabled " + Item.REGISTRY.getNameForObject(output.getItem()).toString() + " (removed recipe)");
				}
			}
		}

		for (Item i : itemSet) {
			ModCharset.logger.info("Disabled " + Item.REGISTRY.getNameForObject(i).toString());
		}
	}
}
