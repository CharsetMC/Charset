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

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;
import pl.asie.charset.lib.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class DyeableItemRecipeFactory implements IRecipeFactory {
	public static class Recipe extends RecipeBase {
		public class DyeIngredient extends IngredientCharset {
			private DyeIngredient() {
				super();
			}

			@Override
			protected ItemStack[][] createMatchingStacks() {
				Collection<ItemStack> stacks = OreDictionary.getOres("dye");
				return new ItemStack[][] { stacks.toArray(new ItemStack[stacks.size()]) };
			}

			@Override
			public boolean hasMatchingStacks() {
				return true;
			}

			@Override
			public boolean matches(ItemStack stack, IRecipeResultBuilder builder) {
				return getColor(stack) != null;
			}
		}

		public final Ingredient DYE = IngredientCharset.wrap(new DyeIngredient());
		public final Ingredient input;

		public Recipe(JsonContext context, JsonObject object, Ingredient ingredient) {
			super(context, object);
			this.input = ingredient;
		}

		public Recipe(String group, Ingredient ingredient) {
			super(group);
			this.input = ingredient;
		}

		protected int[] getColor(ItemStack stack) {
			if (!stack.isEmpty()) {
				if (stack.hasCapability(Capabilities.DYEABLE_ITEM, null)) {
					IDyeableItem targetItem = stack.getCapability(Capabilities.DYEABLE_ITEM, null);

					if (targetItem.hasColor(0)) {
						int c = targetItem.getColor(0);
						return new int[]{(c >> 16) & 255, (c >> 8) & 255, c & 255};
					}
				} else {
					EnumDyeColor dyeId = ColorUtils.getDyeColor(stack);
					if (dyeId != null) {
						float[] col = dyeId.getColorComponentValues();
						return new int[]{
								(int) (col[0] * 255.0F),
								(int) (col[1] * 255.0F),
								(int) (col[2] * 255.0F)
						};
					}
				}
			}

			return null;
		}

		@Override
		public boolean matches(InventoryCrafting inv, World worldIn) {
			ItemStack target = ItemStack.EMPTY;
			List<ItemStack> dyes = new ArrayList<ItemStack>();

			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack source = inv.getStackInSlot(i);
				if (!source.isEmpty()) {
					if (input.apply(source)) {
						if (!target.isEmpty()) {
							return false;
						} else {
							target = source;
						}
					} else if (getColor(source) != null) {
						dyes.add(source);
					} else {
						return false;
					}
				}
			}

			return !target.isEmpty() && !dyes.isEmpty();
		}

		protected Optional<Integer> getMixedColor(InventoryCrafting inv, ItemStack base, Predicate<Integer> slotFilter) {
			int[] color = new int[3];
			int scale = 0;
			int count = 0;

			if (!base.isEmpty()) {
				int[] col = getColor(base);
				if (col != null) {
					scale += Math.max(col[0], Math.max(col[1], col[2]));
					color[0] += col[0];
					color[1] += col[1];
					color[2] += col[2];
					count++;
				}
			}

			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (slotFilter == null || slotFilter.test(i)) {
					ItemStack source = inv.getStackInSlot(i);
					if (!source.isEmpty() && source != base) {
						int[] col = getColor(source);
						if (col != null) {
							scale += Math.max(col[0], Math.max(col[1], col[2]));
							color[0] += col[0];
							color[1] += col[1];
							color[2] += col[2];
							count++;
						}
					}
				}
			}

			if (count > 0) {
				int i1 = color[0] / count;
				int j1 = color[1] / count;
				int k1 = color[2] / count;
				float f3 = (float) scale / (float) count;
				float f4 = (float) Math.max(i1, Math.max(j1, k1));
				i1 = (int) (i1 * f3 / f4);
				j1 = (int) (j1 * f3 / f4);
				k1 = (int) (k1 * f3 / f4);
				return Optional.of((i1 << 16) + (j1 << 8) + k1);
			}

			return Optional.empty();
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			ItemStack target = ItemStack.EMPTY;
			IDyeableItem targetItem = null;

			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack source = inv.getStackInSlot(i);
				if (!source.isEmpty()) {
					if (source.hasCapability(Capabilities.DYEABLE_ITEM, null)) {
						target = source.copy();
						target.setCount(1);
						targetItem = target.getCapability(Capabilities.DYEABLE_ITEM, null);
					}
				}
			}

			if (targetItem != null) {
				Optional<Integer> result = getMixedColor(inv, target, null);
				if (result.isPresent()) {
					targetItem.setColor(0, result.get());
					return target;
				}
			}

			return null;
		}

		@Override
		protected ItemStack toRemainingItem(ItemStack from) {
			if (!from.isEmpty() && from.hasCapability(Capabilities.DYEABLE_ITEM, null)) {
				return ItemStack.EMPTY;
			}

			return super.toRemainingItem(from);
		}

		@Override
		public boolean canFit(int i, int i1) {
			return i * i1 > 1;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return ItemStack.EMPTY;
		}

		@Override
		public NonNullList<Ingredient> getIngredients() {
			return NonNullList.from(input, DYE, DYE);
		}
	}

	@Override
	public IRecipe parse(JsonContext jsonContext, JsonObject jsonObject) {
		Ingredient input = CraftingHelper.getIngredient(jsonObject.get("input"), jsonContext);
		return new Recipe(jsonContext, jsonObject, input);
	}
}
