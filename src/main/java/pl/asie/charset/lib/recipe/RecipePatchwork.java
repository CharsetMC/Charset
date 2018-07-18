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

package pl.asie.charset.lib.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class RecipePatchwork extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	private final Function<ItemStack, ItemStack> transformer;

	public RecipePatchwork(Function<ItemStack, ItemStack> transformer) {
		this.transformer = transformer;
	}

	@Nullable
	private InventoryCrafting getInvPatched(InventoryCrafting inv) {
		InventoryCrafting invPatched = null;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack s = inv.getStackInSlot(i);
			if (!s.isEmpty()) {
				ItemStack st = transformer.apply(s);
				if (st != s) {
					if (invPatched == null) {
						invPatched = new InventoryCraftingPatched(inv.getWidth(), inv.getHeight());

						for (int j = 0; j < inv.getSizeInventory(); j++) {
							invPatched.setInventorySlotContents(j, inv.getStackInSlot(j));
						}
					}

					invPatched.setInventorySlotContents(i, st);
				}
			}
		}

		return invPatched;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		if (!(inv instanceof InventoryCraftingPatched)) {
			InventoryCrafting invPatched = getInvPatched(inv);
			if (invPatched != null) {
				return CraftingManager.findMatchingRecipe(invPatched, worldIn) != null;
			}
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		if (!(inv instanceof InventoryCraftingPatched)) {
			InventoryCrafting invPatched = getInvPatched(inv);
			if (invPatched != null) {
				try {
					return CraftingManager.findMatchingResult(invPatched, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		InventoryCrafting invPatched = getInvPatched(inv);
		InventoryCrafting invPatchedClone = new InventoryCraftingPatched(inv.getWidth(), inv.getHeight());
		for (int i = 0; i < invPatched.getSizeInventory(); i++) {
			ItemStack src = invPatched.getStackInSlot(i);
			invPatchedClone.setInventorySlotContents(i, src.isEmpty() ? ItemStack.EMPTY : src.copy());
		}
		try {
			IRecipe realRecipe = CraftingManager.findMatchingRecipe(invPatched, null);
			if (realRecipe != null) {
				NonNullList<ItemStack> stacks = realRecipe.getRemainingItems(invPatched);
				NonNullList<ItemStack> result = NonNullList.create();
				for (int i = 0; i < stacks.size(); i++) {
					if (invPatched.getStackInSlot(i) == inv.getStackInSlot(i)) {
						result.add(stacks.get(i));
					} else {
						// TODO?
						result.add(ItemStack.EMPTY);
					}
				}
				return result;
			} else {
				return net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inv);
			}
		} catch (Exception e) {
			return net.minecraftforge.common.ForgeHooks.defaultRecipeGetRemainingItems(inv);
		}
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public String getGroup() {
		return "sparta";
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
