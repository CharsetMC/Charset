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

package pl.asie.charset.lib.material;

import com.google.common.base.Joiner;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.utils.RecipeUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class FastRecipeLookup {
	public static boolean ENABLED = true;
	private static boolean IGNORE_NO_SHAPELESS = false;
	private static final Set<Class> saneClasses = new HashSet<>();

	static {
		saneClasses.add(ShapedOreRecipe.class);
		saneClasses.add(ShapedRecipes.class);
		saneClasses.add(RecipeCharset.Shaped.class);
		saneClasses.add(ShapelessOreRecipe.class);
		saneClasses.add(ShapelessRecipes.class);
		saneClasses.add(RecipeCharset.class);
		addSaneClass("forestry.core.recipes.ShapedRecipeCustom");
	}

	private static void addSaneClass(String s) {
		try {
			saneClasses.add(Class.forName(s));
		} catch (Throwable t) {
			//
		}
	}

	public static ItemStack getCraftingResult(InventoryCrafting crafting, World world) {
		IRecipe recipe = findMatchingRecipe(crafting, world);

		if (recipe != null) {
			return recipe.getCraftingResult(crafting);
		} else {
			return ItemStack.EMPTY;
		}
	}

	protected static ItemStack getCraftingResultQuickly(boolean noShapeless, int nonEmptyStacks, World world, int width, int height, ItemStack... stacks) {
		InventoryCrafting crafting = RecipeUtils.getCraftingInventory(width, height, stacks);
		IRecipe recipe = findMatchingRecipeQuickly(!IGNORE_NO_SHAPELESS && noShapeless, nonEmptyStacks, crafting, world);

		if (recipe != null) {
			return recipe.getCraftingResult(crafting);
		} else {
			return ItemStack.EMPTY;
		}
	}

	private static List<Collection<IRecipe>> recipeLists = new ArrayList<>();
	private static TIntObjectMap<Collection<IRecipe>> shapelessOneElement = new TIntObjectHashMap<>();

	public static void clearRecipeLists() {
		recipeLists.clear();
		shapelessOneElement.clear();
	}

	private static void addShapelessOneElement(IRecipe recipe, int i) {
		Collection<IRecipe> list = shapelessOneElement.get(i);
		if (list == null) {
			list = new LinkedHashSet<>();
			shapelessOneElement.put(i, list);
		}
		list.add(recipe);
	}

	private static void addShapelessOneElement(IRecipe recipe) {
		Ingredient ing = recipe.getIngredients().get(0);
		if (ing == Ingredient.EMPTY) {
			addShapelessOneElement(recipe, 0);
		} else {
			Class c = ing.getClass();
			if (c == Ingredient.class || c == IngredientNBT.class) {
				TIntSet set = new TIntHashSet();
				for (ItemStack stack : ing.getMatchingStacks()) {
					int i = toIntIdentifier(stack);
					if (set.add(i)) {
						addShapelessOneElement(recipe, i);
					}
				}
			} else {
				// incomplete recipe, unknown ingredient type
				recipeLists.get(0).add(recipe);
			}
		}
	}

	private static int toIntIdentifier(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		} else {
			return Item.getIdFromItem(stack.getItem());
		}
	}

	public static void initRecipeLists() {
		if (!ENABLED) {
			return;
		}

		if (recipeLists.isEmpty()) {
			// 0-8: shapeless 1-9 ingredients
			// 9-17: shaped 1-9 w/h
			// 18-26: other 1-9 canFit
			// 27: weird
			for (int i = 0; i < 28; i++) {
				recipeLists.add(new ArrayList<>());
			}

			for (IRecipe irecipe : ForgeRegistries.RECIPES) {
				Class c = irecipe.getClass();
				if (saneClasses.contains(c)) {
					if (irecipe instanceof IShapedRecipe) {
						// okay, cool, but is it trimmable?
						// if it's trimmable, it depends on whitespace and is thus "really weird"
						int width = ((IShapedRecipe) irecipe).getRecipeWidth();
						int height = ((IShapedRecipe) irecipe).getRecipeHeight();
						NonNullList<Ingredient> ingredients = irecipe.getIngredients();
						boolean isReallyWeird = false;
						boolean isSlightlyWeird = false;

						if (ingredients.size() != width * height) {
							isReallyWeird = true;
						} else {
							boolean canTrimTop = true;
							boolean canTrimBottom = true;
							boolean canTrimLeft = true;
							boolean canTrimRight = true;

							for (int i = 0; i < width; i++) {
								if (ingredients.get(i) != Ingredient.EMPTY) {
									canTrimTop = false;
								}
								if (ingredients.get(i + (height - 1) * width) != Ingredient.EMPTY) {
									canTrimBottom = false;
								}
							}

							for (int i = 0; i < height; i++) {
								if (ingredients.get(i * width) != Ingredient.EMPTY) {
									canTrimLeft = false;
								}
								if (ingredients.get(i * width + (width - 1)) != Ingredient.EMPTY) {
									canTrimRight = false;
								}
							}

							if (canTrimTop || canTrimLeft || canTrimRight || canTrimBottom) {
								isSlightlyWeird = true;
							}
						}

						if (isReallyWeird) {
							recipeLists.get(27).add(irecipe);
						} else if (isSlightlyWeird) {
							recipeLists.get(18 + (width - 1) * 3 + (height - 1)).add(irecipe);
						} else {
							if (width * height == 1) {
								addShapelessOneElement(irecipe);
							} else {
								int wh = (width - 1) * 3 + (height - 1);
								recipeLists.get(9 + wh).add(irecipe);
							}
						}
					} else {
						NonNullList<Ingredient> ings = irecipe.getIngredients();
						boolean isReallyWeird = false;
						for (Ingredient i : ings) {
							if (i == Ingredient.EMPTY) {
								isReallyWeird = true;
								break;
							}
						}
						if (isReallyWeird) {
							recipeLists.get(27).add(irecipe);
						} else {
							if (ings.size() == 1) {
								addShapelessOneElement(irecipe);
							} else {
								recipeLists.get(ings.size() - 1).add(irecipe);
							}
						}
					}
				} else {
					if (!irecipe.canFit(4, 4)) {
						// always false?
						recipeLists.get(27).add(irecipe);
					} else {
						int smallestFittable = 10;
						for (int iw = 0; iw <= 3; iw++) {
							for (int ih = 0; ih <= 3; ih++) {
								if (irecipe.canFit(iw, ih)) {
									int i = iw*ih;
									if (smallestFittable > i) {
										smallestFittable = i;
									}
								}
							}
						}

						if (smallestFittable <= 0 || smallestFittable > 9) {
							recipeLists.get(27).add(irecipe);
						} else {
							recipeLists.get(18 + (smallestFittable - 1)).add(irecipe);
						}
					}
				}
			}

			if (CharsetLib.enableDebugInfo) {
				try {
					File outputFile = new File("charsetFastRecipeLookup.txt");
					PrintWriter writer = new PrintWriter(outputFile);
					writer.println("SHAPELESS ONE ELEMENT = " + shapelessOneElement.size() + " lists");
					for (int i = 0; i < 9; i++) {
						writer.println("SHAPELESS " + (i + 1) + " = " + recipeLists.get(i).size());
					}
					for (int i = 0; i < 9; i++) {
						writer.println("SHAPED " + ((i % 3) + 1) + "x" + ((i / 3) + 1) + " = " + recipeLists.get(i + 9).size());
					}
					for (int i = 0; i < 9; i++) {
						writer.println("WEIRD " + (i + 1) + " = " + recipeLists.get(i + 18).size());
					}
					writer.println("REALLY WEIRD = " + recipeLists.get(27).size());

					for (int i = 0; i <= 9; i++) {
						Collection<IRecipe> recipes = recipeLists.get(i + 18);
						TObjectIntMap<Class> classMap = new TObjectIntHashMap<>();
						String name = i == 9 ? "REALLY WEIRD" : ("WEIRD " + (i + 1));
						for (IRecipe recipe : recipes) {
							classMap.adjustOrPutValue(recipe.getClass(), 1, 1);
						}
						for (Class c : classMap.keySet()) {
							writer.println("[" + name + "] " + classMap.get(c) + "x" + c.getName());
						}
					}

					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn) {
		if (!ENABLED) {
			return CraftingManager.findMatchingRecipe(craftMatrix, worldIn);
		}

		// trim craft matrix
		int x = 0;
		int y = 0;
		int width = craftMatrix.getWidth();
		int height = craftMatrix.getHeight();
		boolean empty;

		// trim right
		empty = true;
		while (empty) {
			for (int i = 0; i < height; i++) {
				if (!craftMatrix.getStackInRowAndColumn(width-1, i).isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty) {
				width--;
				if (width == 0) {
					return null;
				}
			}
		}

		// trim bottom
		empty = true;
		while (empty) {
			for (int i = 0; i < width; i++) {
				if (!craftMatrix.getStackInRowAndColumn(i, height-1).isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty) {
				height--;
				if (height == 0) {
					return null;
				}
			}
		}

		// trim left
		empty = true;
		while (empty) {
			for (int i = 0; i < height; i++) {
				if (!craftMatrix.getStackInRowAndColumn(x, i).isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty) {
				x++;
				width--;
			}
		}

		// trim top
		empty = true;
		while (empty) {
			for (int i = 0; i < width; i++) {
				if (!craftMatrix.getStackInRowAndColumn(x + i, y).isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty) {
				y++;
				height--;
			}
		}

		// create trimmed matrix
		if (width == craftMatrix.getWidth() && height == craftMatrix.getHeight()) {
			int nonEmptyStacks = 0;
			for (int iy = 0; iy < height; iy++) {
				for (int ix = 0; ix < width; ix++) {
					ItemStack stack = craftMatrix.getStackInRowAndColumn(x + ix, y + iy);
					if (!stack.isEmpty()) {
						nonEmptyStacks++;
					}
				}
			}
			return findMatchingRecipeQuickly(false, nonEmptyStacks, craftMatrix, craftMatrix, worldIn);
		} else {
			InventoryCrafting craftingTrimmed = RecipeUtils.getCraftingInventory(width, height, RecipeUtils.getContainer(craftMatrix));
			int nonEmptyStacks = 0;
			for (int iy = 0; iy < height; iy++) {
				for (int ix = 0; ix < width; ix++) {
					ItemStack stack = craftMatrix.getStackInRowAndColumn(x + ix, y + iy);
					if (!stack.isEmpty()) {
						nonEmptyStacks++;
					}
					craftingTrimmed.setInventorySlotContents(iy * width + ix, stack);
				}
			}
			return findMatchingRecipeQuickly(false, nonEmptyStacks, craftingTrimmed, craftMatrix, worldIn);
		}
	}

	protected static IRecipe findMatchingRecipeQuickly(boolean noShapeless, int nonEmptyStacks, InventoryCrafting craftMatrix, World worldIn) {
		return findMatchingRecipeQuickly(noShapeless, nonEmptyStacks, craftMatrix, null, worldIn);
	}

	protected static IRecipe findMatchingRecipeQuickly(boolean noShapeless, int nonEmptyStacks, InventoryCrafting craftMatrix, @Nullable InventoryCrafting craftMatrixUntrimmed, World worldIn) {
		if (!ENABLED) {
			return CraftingManager.findMatchingRecipe(craftMatrix, worldIn);
		}

		initRecipeLists();

		int width = craftMatrix.getWidth();
		int height = craftMatrix.getHeight();

		// shaped tree
		if ((width * height) >= 2) {
			for (IRecipe irecipe : recipeLists.get(9 + (width - 1) * 3 + (height - 1))) {
				if (irecipe.matches(craftMatrix, worldIn)) {
					return irecipe;
				}
			}
		}

		if (nonEmptyStacks == 1) {
			ItemStack stack = ItemStack.EMPTY;
			for (int i = 0; i < (width * height); i++) {
				stack = craftMatrix.getStackInSlot(i);
				if (!stack.isEmpty()) break;
			}

			Collection<IRecipe> list = shapelessOneElement.get(toIntIdentifier(stack));
			if (list != null) {
				for (IRecipe recipe : list) {
					if (recipe.matches(craftMatrix, worldIn)) {
						return recipe;
					}
				}
			}

			for (IRecipe recipe : recipeLists.get(0)) {
				if (recipe.matches(craftMatrix, worldIn)) {
					return recipe;
				}
			}
		} else if (!noShapeless && nonEmptyStacks > 1) {
			for (IRecipe irecipe : recipeLists.get(nonEmptyStacks - 1)) {
				if (irecipe.matches(craftMatrix, worldIn)) {
					return irecipe;
				}
			}
		}

		// first, check recipes which are smaller
		int base = width * height - 1;
		for (int r = base; r >= 0; r--) {
			for (IRecipe irecipe : recipeLists.get(18 + r)) {
				if (!irecipe.canFit(width, height)) {
					continue;
				}

				if (irecipe.matches(craftMatrix, worldIn)) {
					return irecipe;
				}
			}
		}

		// then, check recipes which are larger (just in case)
		InventoryCrafting craftMatrixWeird = craftMatrixUntrimmed != null ? craftMatrixUntrimmed : craftMatrix;

		for (int r = base + 1; r < 9; r++) {
			for (IRecipe irecipe : recipeLists.get(18 + r)) {
				if (!irecipe.canFit(craftMatrixWeird.getWidth(), craftMatrixWeird.getHeight())) {
					continue;
				}

				if (irecipe.matches(craftMatrixWeird, worldIn)) {
					return irecipe;
				}
			}
		}

		// and finally, the true oddballs
		for (IRecipe irecipe : recipeLists.get(27)) {
			if (irecipe.matches(craftMatrixWeird, worldIn)) {
				return irecipe;
			}
		}

		return null;
	}
}
