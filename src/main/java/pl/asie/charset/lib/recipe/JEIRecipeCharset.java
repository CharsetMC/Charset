package pl.asie.charset.lib.recipe;

import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JEIRecipeCharset extends BlankRecipeWrapper implements ICraftingRecipeWrapper {
    public static class Handler implements IRecipeHandler<RecipeCharset> {
        @Nonnull
        @Override
        public Class<RecipeCharset> getRecipeClass() {
            return RecipeCharset.class;
        }

        @Nonnull
        @Override
        public String getRecipeCategoryUid() {
            return VanillaRecipeCategoryUid.CRAFTING;
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
    public List getInputs() {
        List<Object> mats = new ArrayList<Object>();
        for (IRecipeObject o : recipe.input) {
            mats.add(o != null ? o.preview() : null);
        }

        return mats;
    }

    @Override
    public List<ItemStack> getOutputs() {
        Object o = recipe.output.preview();
        return o instanceof List ? (List<ItemStack>) o : (o instanceof ItemStack ? Collections.singletonList((ItemStack) o) : null);
    }
}
