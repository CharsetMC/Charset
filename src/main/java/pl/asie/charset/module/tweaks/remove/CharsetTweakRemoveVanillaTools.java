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

import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@CharsetModule(
		name = "tweak.remove.vanillaStyleTools",
		profile = ModuleProfile.STABLE,
		isDefault = false
)
public class CharsetTweakRemoveVanillaTools {
	public int getMode() {
		return 2;
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Set<Item> itemSet = new HashSet<Item>();
		for (ResourceLocation l : Item.REGISTRY.getKeys()) {
			Item i = Item.REGISTRY.getObject(l);
			if (i instanceof ItemPickaxe || i instanceof ItemAxe || i instanceof ItemSpade || i instanceof ItemSword) {
				i.setMaxDamage(1);
				itemSet.add(i);
			}
		}
		if (getMode() >= 2) {
			Iterator<IRecipe> iterator = CraftingManager.REGISTRY.iterator();
			while (iterator.hasNext()) {
				ItemStack output = iterator.next().getRecipeOutput();
				if (!output.isEmpty() && itemSet.contains(output.getItem())) {
					iterator.remove();
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
