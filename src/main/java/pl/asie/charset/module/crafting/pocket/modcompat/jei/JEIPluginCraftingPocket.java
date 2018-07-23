/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.crafting.pocket.modcompat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.module.crafting.pocket.CharsetCraftingPocket;
import pl.asie.charset.module.crafting.pocket.GuiPocketTable;

@CharsetJEIPlugin("crafting.pocket")
public class JEIPluginCraftingPocket implements IModPlugin {
	@Override
	public void register(IModRegistry registry) {
		registry.addRecipeCatalyst(new ItemStack(CharsetCraftingPocket.pocketTable), VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeClickArea(GuiPocketTable.class, 177, 25, 25, 22, VanillaRecipeCategoryUid.CRAFTING);

		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
		recipeTransferRegistry.addRecipeTransferHandler(new PocketRecipeTransferInfo());
	}
}
