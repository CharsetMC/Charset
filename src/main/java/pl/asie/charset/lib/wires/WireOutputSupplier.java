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

package pl.asie.charset.lib.wires;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IOutputSupplierFactory;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

public class WireOutputSupplier implements IOutputSupplier {
	private final ResourceLocation id;
	private final boolean freestanding;
	private final int amount;

	public WireOutputSupplier(ResourceLocation id, boolean freestanding, int amount) {
		this.id = id;
		this.freestanding = freestanding;
		this.amount = amount;
	}

	@Override
	public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
		return getDefaultOutput();
	}

	@Override
	public ItemStack getDefaultOutput() {
		if (!WireManager.REGISTRY.containsKey(id)) {
			return ItemStack.EMPTY;
		}

		return CharsetLibWires.itemWire.toStack(WireManager.REGISTRY.getValue(id), freestanding, amount);
	}

	public static class Factory implements IOutputSupplierFactory {
		@Override
		public IOutputSupplier parse(JsonContext context, JsonObject json) {
			ResourceLocation id = new ResourceLocation(JsonUtils.getString(json, "id"));
			boolean freestanding = JsonUtils.getBoolean(json, "freestanding", false);
			int amount = JsonUtils.getInt(json, "count", 1);
			return new WireOutputSupplier(id, freestanding, amount);
		}
	}
}

