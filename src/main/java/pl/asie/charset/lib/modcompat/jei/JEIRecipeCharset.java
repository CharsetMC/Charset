package pl.asie.charset.lib.modcompat.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.recipe.IRecipeObject;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class JEIRecipeCharset extends BlankRecipeWrapper implements IRecipeWrapper {
    public static class Handler implements IRecipeHandler<RecipeCharset> {
        @Nonnull
        @Override
        public Class<RecipeCharset> getRecipeClass() {
            return RecipeCharset.class;
        }

        @Nonnull
        @Override
        public String getRecipeCategoryUid(@Nonnull RecipeCharset recipe) {
            return VanillaRecipeCategoryUid.CRAFTING;
        }

        @Nonnull
        @Override
        public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeCharset recipe) {
            return create(recipe);
        }

        @Override
        public boolean isRecipeValid(@Nonnull RecipeCharset recipe) {
            return true;
        }
    }

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
            return recipe.width;
        }

        @Override
        public int getHeight() {
            return recipe.height;
        }
    }

    public static JEIRecipeCharset create(RecipeCharset recipe) {
        return recipe.shapeless ? new Shapeless(recipe) : new Shaped(recipe);
    }

    protected final RecipeCharset recipe;

    private JEIRecipeCharset(RecipeCharset recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<Object> inputs = new ArrayList<Object>();
        Object output = recipe.output.preview();

        for (IRecipeObject o : recipe.input) {
            inputs.add(o != null ? o.preview() : null);
        }

        ingredients.setInputLists(ItemStack.class, JEIPluginCharsetLib.STACKS.expandRecipeItemStackInputs(inputs));
        if (output instanceof ItemStack) {
            ingredients.setOutputs(ItemStack.class, JEIPluginCharsetLib.STACKS.getSubtypes((ItemStack) output));
        } else if (output instanceof List) {
            ingredients.setOutputs(ItemStack.class, (List<ItemStack>) output);
        }
    }
}
