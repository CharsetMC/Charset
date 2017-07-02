package pl.asie.charset.lib.recipe;

import com.google.common.collect.Sets;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCustomHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.ItemStackHashSet;

import javax.annotation.Nullable;
import java.util.*;

// TODO: Add option to replace with OreIngredients
// TODO: Add support for IngredientNBTs? Smelting? Brewing?
public class RecipeReplacement {
    public static final RecipeReplacement PRIMARY = new RecipeReplacement();

    private final Map<ItemStack, Object> replacements = new TCustomHashMap<>(
            new ItemStackHashSet.Strategy(false, true, true)
    );
    private final Map<Item, Object> replaceableItems = new IdentityHashMap<>();

    public RecipeReplacement() {

    }

    public void add(ItemStack from, ItemStack to) {
        if (from.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            replaceableItems.put(from.getItem(), to.getItem());
        } else {
            replacements.put(from, to);
        }
    }

    public void add(Item from, Item to) {
        replaceableItems.put(from, to);
    }

    @Nullable
    private Ingredient replaceIngredient(Ingredient ing) {
        if (ing.getClass() == Ingredient.class) {
            ItemStack[] matchingStacks = ing.getMatchingStacks();
            ItemStack[] matchingStacksNew = null;
            boolean dirty = false;

            for (int j = 0; j < matchingStacks.length; j++) {
                ItemStack stack = matchingStacks[j];
                ItemStack newStack = null;
                Object replacement = null;

                if (replaceableItems.containsKey(stack.getItem())) {
                    replacement = replaceableItems.get(stack.getItem());
                } else if (replacements.containsKey(stack)) {
                    replacement = replacements.get(stack);
                }

                if (replacement instanceof Item) {
                    newStack = new ItemStack((Item) replacement, stack.getCount(), stack.getItemDamage());
                    newStack.setTagCompound(stack.getTagCompound());
                } else if (replacement instanceof ItemStack) {
                    newStack = ((ItemStack) replacements.get(stack)).copy();
                    newStack.setCount(stack.getCount());
                } else if (replacement instanceof String) {
                    // TODO: Handle creating OreIngredients
                }

                if (newStack != null) {
                    if (!dirty) {
                        matchingStacksNew = new ItemStack[matchingStacks.length];
                        System.arraycopy(matchingStacks, 0, matchingStacksNew, 0, matchingStacks.length);
                        dirty = true;
                    }

                    matchingStacksNew[j] = newStack;
                }
            }

            if (matchingStacksNew != null) {
                return Ingredient.fromStacks(matchingStacksNew);
            }
        }

        return null;
    }

    public void process(Collection<IRecipe> registry) {
        for (IRecipe recipe : registry) {
            ResourceLocation recipeName = recipe.getRegistryName();
            boolean dirty = false;

            if (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    Ingredient ing = ingredients.get(i);
                    Ingredient ingNew = replaceIngredient(ing);
                    if (ingNew != null) {
                        ingredients.set(i, ingNew);
                        dirty = true;
                    }
                }
            } else if (recipe instanceof RecipeCharset) {
                TCharObjectMap<Ingredient> charToIngredient = ((RecipeCharset) recipe).charToIngredient;
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    Ingredient ing = ingredients.get(i);
                    Ingredient ingNew = replaceIngredient(ing);
                    if (ingNew != null) {
                        ingredients.set(i, ingNew);
                        TCharIterator iterator = charToIngredient.keySet().iterator();
                        while (iterator.hasNext()) {
                            char c = iterator.next();
                            if (charToIngredient.get(c) == ing) {
                                charToIngredient.put(c, ing);
                            }
                        }
                        dirty = true;
                    }
                }
            }

            if (dirty) {
                ModCharset.logger.info("Successfully edited " + recipeName + "!");
            }
        }
    }
}
