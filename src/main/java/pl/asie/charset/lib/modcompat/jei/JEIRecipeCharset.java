package pl.asie.charset.lib.modcompat.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class JEIRecipeCharset implements IRecipeWrapper {
    public static class Shapeless extends JEIRecipeCharset {
        public Shapeless(RecipeCharset recipe) {
            super(recipe);
        }
    }

    public static class Shaped extends JEIRecipeCharset implements IShapedCraftingRecipeWrapper {
        public Shaped(RecipeCharset recipe) {
            super(recipe);
        }

        @Override
        public int getWidth() {
            return recipe.getWidth();
        }

        @Override
        public int getHeight() {
            return recipe.getHeight();
        }
    }

    public static JEIRecipeCharset create(RecipeCharset recipe) {
        return recipe.getType() == RecipeCharset.Type.SHAPELESS ? new Shapeless(recipe) : new Shaped(recipe);
    }

    protected final RecipeCharset recipe;

    private JEIRecipeCharset(RecipeCharset recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, JEIPluginCharset.STACKS.expandRecipeItemStackInputs(recipe.getIngredients()));
        ingredients.setOutputs(ItemStack.class, recipe.getExampleOutputs());
    }
}
