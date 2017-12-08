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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.TObjectCharMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.map.hash.TObjectCharHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.ingredient.IRecipeView;
import pl.asie.charset.lib.recipe.ingredient.IngredientWrapper;
import pl.asie.charset.lib.utils.ItemStackHashSet;

import javax.annotation.Nullable;
import java.util.*;

public class RecipeCharset extends RecipeBase implements IRecipeView {
    public static class Shaped extends RecipeCharset implements IShapedRecipe {
        public Shaped(JsonContext context, JsonObject object) {
            super(context, object);
        }

        public Shaped(String group) {
            super(group);
        }

        public Shaped(String group, boolean hidden) {
            super(group, hidden);
        }

        @Override
        public int getRecipeWidth() {
            return getWidth();
        }

        @Override
        public int getRecipeHeight() {
            return getHeight();
        }
    }

    public enum Type {
        SHAPED,
        SHAPELESS
    }

    public RecipeCharset(JsonContext context, JsonObject object) {
        super(context, object);
    }

    public RecipeCharset(String group) {
        super(group);
    }

    public RecipeCharset(String group, boolean hidden) {
        super(group, hidden);
    }

    protected final boolean shapeless = !(this instanceof Shaped);
    protected IOutputSupplier output;
    protected final TCharObjectMap<Ingredient> charToIngredient = new TCharObjectHashMap<>();
    protected int[] shapedOrdering;
    protected NonNullList<Ingredient> input = null;
    protected int width = 0;
    protected int height = 0;
    protected boolean mirrored = false;

    public Collection<ItemStack> getAllRecipeOutputs() {
        InventoryCraftingIterator inventoryCrafting = new InventoryCraftingIterator(this, true);
        ItemStackHashSet stackSet = new ItemStackHashSet(true, true, true);
        List<ItemStack> stacks = new ArrayList<>();

        InventoryCraftingIterator it = inventoryCrafting;
        while (it.hasNext()) {
            InventoryCrafting ic = it.next();
            ItemStack stack = getCraftingResult(ic);
            if (!stack.isEmpty() && stackSet.add(stack)) {
                stacks.add(stack);
            }
        }

        return stacks;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return input;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Type getType() {
        return shapeless ? Type.SHAPELESS : Type.SHAPED;
    }

    protected IngredientMatcher matchedOrNull(InventoryCrafting inv) {
        if (shapeless) {
            Set<Ingredient> objectSet = new HashSet<>();
            objectSet.addAll(input);
            IngredientMatcher matcher = new IngredientMatcher(this);

            for (int y = 0; y < inv.getHeight(); y++) {
                for (int x = 0; x < inv.getWidth(); x++) {
                    ItemStack stack = inv.getStackInRowAndColumn(x, y);
                    if (!stack.isEmpty()) {
                        boolean matches = false;

                        for (Ingredient o : objectSet) {
                            if (o.apply(stack)) {
                                matches = true;
                                matcher.add(stack, o);
                                objectSet.remove(o);
                                break;
                            }
                        }

                        if (!matches) {
                            return null;
                        }
                    }
                }
            }

            return objectSet.size() == 0 ? matcher : null;
        } else {
            for (int yo = 0; yo <= inv.getHeight() - height; yo++) {
                for (int xo = 0; xo <= inv.getWidth() - width; xo++) {
                    IngredientMatcher matcher = new IngredientMatcher(this);
                    boolean matches = false;

                    for (int iIdx = 0; iIdx < input.size(); iIdx++) {
                        int i = (shapedOrdering != null ? shapedOrdering[iIdx] : iIdx);
                        int x = i % width + xo;
                        int y = i / width + yo;
                        matches = matcher.add(inv.getStackInRowAndColumn(x, y), input.get(i));

                        if (!matches) break;
                    }

                    if (!matches && mirrored) {
                        matcher = new IngredientMatcher(this);
                        matches = false;

                        for (int iIdx = 0; iIdx < input.size(); iIdx++) {
                            int i = (shapedOrdering != null ? shapedOrdering[iIdx] : iIdx);
                            int x = ((width - 1) - (i % width)) + xo;
                            int y = i / width + yo;
                            matches = matcher.add(inv.getStackInRowAndColumn(x, y), input.get(i));

                            if (!matches) break;
                        }
                    }

                    if (matches) return matcher;
                }
            }

            return null;
        }
    }

    @Override
    public boolean matches(InventoryCrafting inv, @Nullable World worldIn) {
        return matchedOrNull(inv) != null;
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        IngredientMatcher matcher = matchedOrNull(inv);
        if (matcher != null) {
            return matcher.apply(output.getCraftingResult(this, matcher, inv));
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canFit(int i, int i1) {
        return shapeless ? (i * i1) >= input.size() : this.width >= i && this.height >= i1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output.getDefaultOutput();
    }

    @Override
    public Ingredient getIngredient(char c) {
        return charToIngredient.get(c);
    }

    public static class Factory implements IRecipeFactory {
        protected String getType(JsonContext context, JsonObject json) {
            return JsonUtils.getString(json, "type");
        }

        protected Ingredient parseIngredient(JsonElement json, JsonContext context) {
            return CraftingHelper.getIngredient(json, context);
        }

        protected void parseInputShapeless(RecipeCharset recipe, JsonContext context, JsonObject json) {
            JsonArray array = JsonUtils.getJsonArray(json, "ingredients");
            recipe.input = NonNullList.create();
            for (int i = 0; i < array.size(); i++) {
                recipe.input.add(parseIngredient(array.get(i), context));
            }
            recipe.width = recipe.input.size();
            recipe.height = 1;
        }

        protected void parseInputShaped(RecipeCharset recipe, JsonContext context, JsonObject json) {
            recipe.mirrored = JsonUtils.getBoolean(json, "mirrored", false);

            List<String> shape = new ArrayList<>();

            JsonArray pattern = JsonUtils.getJsonArray(json, "pattern");
            for (int idx = 0; idx < pattern.size(); idx++) {
                String s = pattern.get(idx).getAsString();
                shape.add(s);
                recipe.width = Math.max(recipe.width, s.length());
            }
            recipe.height = pattern.size();

            recipe.charToIngredient.put(' ', Ingredient.EMPTY);
            TObjectCharMap<Ingredient> ingredientToChar = new TObjectCharHashMap<>();

            JsonObject key = JsonUtils.getJsonObject(json, "key");
            for (Map.Entry<String, JsonElement> entry : key.entrySet()) {
                if (entry.getKey().length() > 1) {
                    throw new RuntimeException("Invalid recipe key: '" + entry.getKey() + "'!");
                }
                char c = entry.getKey().charAt(0);
                recipe.charToIngredient.put(c, parseIngredient(entry.getValue(), context));
                ingredientToChar.put(recipe.charToIngredient.get(c), c);
            }

            recipe.shapedOrdering = new int[recipe.width * recipe.height];

            recipe.input = NonNullList.create();

            for (int y = 0; y < recipe.height; y++) {
                String s = shape.get(y);
                for (int x = 0; x < recipe.width; x++) {
                    if (x < s.length()) {
                        Ingredient i = recipe.charToIngredient.get(s.charAt(x));
                        if (i == null) {
                            throw new RuntimeException("IngredientMaterial not found: '" + s.charAt(x) + "'!");
                        }
                        recipe.input.add(i);
                    } else {
                        recipe.input.add(Ingredient.EMPTY);
                    }
                }
            }

            Set<Ingredient> addedIngredients = new HashSet<>();
            TIntSet addedPositions = new TIntHashSet();

            int prevIdx;
            int idx = 0;
            while (idx < recipe.shapedOrdering.length) {
                prevIdx = idx;

                for (int i = 0; i < recipe.input.size(); i++) {
                    if (!addedPositions.contains(i)) {
                        Ingredient ingredient = recipe.input.get(i);
                        TCharSet deps = ingredient instanceof IngredientWrapper ? ((IngredientWrapper) ingredient).getIngredientCharset().getDependencies() : null;
                        if (deps != null && deps.size() > 0) {
                            boolean match = true;
                            TCharIterator it = deps.iterator();
                            while (it.hasNext()) {
                                if (!addedIngredients.contains(recipe.charToIngredient.get(it.next()))) {
                                    match = false;
                                    break;
                                }
                            }

                            if (!match) {
                                continue;
                            }
                        }

                        recipe.shapedOrdering[idx++] = i;
                        addedIngredients.add(ingredient);
                        addedPositions.add(i);
                    }
                }

                if (prevIdx == idx) {
                    throw new RuntimeException("Cyclic dependency detected!");
                }
            }
        }

        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            RecipeCharset recipe;
            String type = getType(context, json);

            if (type.endsWith("shapeless")) {
                recipe = new RecipeCharset(context, json);
                parseInputShapeless(recipe, context, json);
            } else if (type.endsWith("shaped")) {
                recipe = new RecipeCharset.Shaped(context, json);
                parseInputShaped(recipe, context, json);
            } else {
                throw new RuntimeException("Unknown type: " + type);
            }

            recipe.output = OutputSupplier.createOutputSupplier(context, JsonUtils.getJsonObject(json, "result"));

            for (Ingredient ing : recipe.getIngredients()) {
                if (ing instanceof IngredientWrapper) {
                    ((IngredientWrapper) ing).getIngredientCharset().onAdded(recipe);
                }
            }
            return recipe;
        }
    }
}
