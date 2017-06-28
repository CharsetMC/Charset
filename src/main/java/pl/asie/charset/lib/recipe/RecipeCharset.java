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
import gnu.trove.set.hash.TCharHashSet;
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
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nullable;
import java.util.*;

public class RecipeCharset extends RecipeBase implements IngredientMatcher.Container {
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

    protected final TCharObjectMap<Ingredient> charToIngredient = new TCharObjectHashMap<>();
    protected int[] shapedOrdering;
    protected NonNullList<Ingredient> input = null;
    protected ItemStack output;
    protected int width = 0;
    protected int height = 0;
    protected boolean mirrored = false;
    protected boolean shapeless = false;

    public List<ItemStack> getExampleOutputs() {
        InventoryCraftingIterator inventoryCrafting = new InventoryCraftingIterator(this, true);
        List<ItemStack> exampleOutputs = new ArrayList<>();

        InventoryCraftingIterator it = inventoryCrafting;
        while (it.hasNext()) {
            InventoryCrafting ic = it.next();
            ItemStack stack = getCraftingResult(ic);
            if (!stack.isEmpty()) {
                exampleOutputs.add(stack);
            }
        }

        return exampleOutputs;
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

            for (int y = 0; y < inv.getHeight(); y++) {
                for (int x = 0; x < inv.getWidth(); x++) {
                    ItemStack stack = inv.getStackInRowAndColumn(x, y);
                    if (!stack.isEmpty()) {
                        boolean matches = false;

                        for (Ingredient o : objectSet) {
                            if (o.apply(stack)) {
                                matches = true;
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

            return objectSet.size() == 0 ? new IngredientMatcher(this) : null;
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
            return matcher.apply(output.copy());
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
        return output;
    }

    @Override
    public Ingredient getIngredient(char c) {
        return charToIngredient.get(c);
    }

    public static class Factory implements IRecipeFactory {
        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            RecipeCharset recipe = new RecipeCharset(context, json);
            String type = JsonUtils.getString(json, "type");

            if (type.equals("charset:shapeless")) {
                recipe.shapeless = true;
                JsonArray array = JsonUtils.getJsonArray(json, "ingredients");
                recipe.input = NonNullList.create();
                for (int i = 0; i < array.size(); i++) {
                    recipe.input.add(CraftingHelper.getIngredient(array.get(i), context));
                }
                recipe.width = recipe.input.size();
                recipe.height = 1;
            } else if (type.equals("charset:shaped")) {
                recipe.shapeless = false;
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
                    recipe.charToIngredient.put(c, CraftingHelper.getIngredient(entry.getValue(), context));
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
                                throw new RuntimeException("Ingredient not found: '" + s.charAt(x) + "'!");
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
                            TCharSet deps = ingredient instanceof IngredientCharset ? ((IngredientCharset) ingredient).getDependencies() : null;
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

                            if (!addedIngredients.contains(ingredient) && ingredient instanceof IngredientCharset) {
                                for (Ingredient ing : addedIngredients) {
                                    ((IngredientCharset) ingredient).addDependency(ingredientToChar.get(ing), ing);
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
            } else {
                throw new RuntimeException("Unknown type: " + type);
            }

            recipe.output = CraftingHelper.getItemStack(json.getAsJsonObject("result"), context);
            return recipe;
        }
    }
}
