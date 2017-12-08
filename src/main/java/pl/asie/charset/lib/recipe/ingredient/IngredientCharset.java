package pl.asie.charset.lib.recipe.ingredient;

import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import pl.asie.charset.lib.utils.ItemUtils;

/**
 * Charset's replacement Ingredient class.
 *
 * Use IngredientCharset.wrap() to create a Minecraft-compatible Ingredient.
 * Use RecipeCharset (charset:shaped, charset:shapeless) to create IRecipes
 * which are capable of utilizing additional functionality in this class.
 * (The Ingredient wrapper will try to gracefully fall back when necessary.)
 */
public abstract class IngredientCharset {
    private static final TCharSet EMPTY_CHAR_SET = new TCharHashSet();

    public IngredientCharset() {
    }

    /**
     * Called when the Ingredient has been added to an IRecipe.
     * The intention is to allow looking up informations about
     * potential co-dependent Ingredients (see getDependencies()).
     *
     * @param view The recipe's view.
     */
    public void onAdded(IRecipeView view) {

    }

    /**
     * Are this recipe's permutations distinct?
     *
     * This, among others, governs recipe preview behaviour in JEI
     * as well as whether two different stacks in two different recipe
     * slots under the same ingredient object can be used to craft
     * the item. (If true, they cannot - the stacks must all be mergeable.)
     */
    public boolean arePermutationsDistinct() {
        return false;
    }

    /**
     * Do these two ItemStacks match in the context of the same
     * recipe grid? It can be safely assumed that both already fulfill
     * IngredientCharset.matches().
     */
    public boolean matchSameGrid(ItemStack a, ItemStack b) {
        return !arePermutationsDistinct() || ItemUtils.canMerge(a, b);
    }

    /**
     * Does this ItemStack match in the context of this Ingredient?
     *
     * The addition here is the IRecipeResultBuilder, letting you look up
     * what exists in other ingredients. (This behaviour is only
     * safe for characters previously defined in getDependencies())
     */
    public abstract boolean matches(ItemStack stack, IRecipeResultBuilder builder);

    /**
     * Transform a given ItemStack based on the ItemStack fed to this ingredient.
     * Can be used to, for example, append NBT data based on the specific ItemStack
     * matched.
     *
     * @param stack The stack to be transformed.
     * @param source The matched stack in the recipe grid slot.
     * @param builder The current recipe result builder.
     * @return A transformation of the "stack" parameter.
     */
    public ItemStack transform(ItemStack stack, ItemStack source, IRecipeResultBuilder builder) {
        return stack;
    }

    /**
     * @return An array of matching ItemStacks.
     */
    public abstract ItemStack[] getMatchingStacks();

    /**
     * @return A set of characters which signify Ingredients this ingredient
     * wishes to look up the state for.
     */
    public TCharSet getDependencies() {
        return EMPTY_CHAR_SET;
    }

    /**
     * Magical wrapping logic!
     *
     * @param ingredient A Charset Ingredient.
     * @return A Minecraft-compatible Ingredient.
     */
    public static final Ingredient wrap(IngredientCharset ingredient) {
        return new IngredientWrapper(ingredient);
    }
}
