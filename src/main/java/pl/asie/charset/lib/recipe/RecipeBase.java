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

package pl.asie.charset.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.utils.ThreeState;

public abstract class RecipeBase extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	private final String group;
	private final ThreeState dynamic;

	public RecipeBase(JsonContext context, JsonObject object) {
		if (object != null && context != null) {
			if (object.has("group")) {
				group = JsonUtils.getString(object, "group");
			} else {
				group = "";
			}

			if (object.has("dynamic")) {
				dynamic = JsonUtils.getBoolean(object, "dynamic") ? ThreeState.YES : ThreeState.NO;
			} else {
				dynamic = ThreeState.MAYBE;
			}
		} else {
			group = "";
			dynamic = ThreeState.MAYBE;
		}
	}

	public RecipeBase(String group) {
		this.group = group;
		this.dynamic = ThreeState.MAYBE;
	}

	public RecipeBase(String group, boolean dynamic) {
		this.group = group;
		this.dynamic = dynamic ? ThreeState.YES : ThreeState.NO;
	}

	@Override
	public String getGroup() {
		return group;
	}

	protected ItemStack toRemainingItem(ItemStack from) {
		return ForgeHooks.getContainerItem(from);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < ret.size(); i++) {
			ret.set(i, toRemainingItem(inv.getStackInSlot(i)));
		}
		return ret;
	}

	@Override
	public boolean isDynamic() {
		if (dynamic == ThreeState.MAYBE) {
			return !getRecipeOutput().isEmpty();
		} else {
			return dynamic == ThreeState.YES;
		}
	}
}
