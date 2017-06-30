package pl.asie.charset.lib.modcompat.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.recipe.DyeableItemRecipeFactory;
import pl.asie.charset.lib.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Collections;

public class JEIRecipeDyeableItem implements IRecipeWrapper {
    public static JEIRecipeDyeableItem create(DyeableItemRecipeFactory.Recipe recipe) {
        return new JEIRecipeDyeableItem(recipe);
    }

    protected final DyeableItemRecipeFactory.Recipe recipe;

    private JEIRecipeDyeableItem(DyeableItemRecipeFactory.Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, JEIPluginCharset.STACKS.expandRecipeItemStackInputs(
                Lists.newArrayList(recipe.input, recipe.DYE)
        ));

        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : recipe.input.getMatchingStacks()) {
            IDyeableItem item = (IDyeableItem) stack.getItem();
            for (EnumDyeColor color : EnumDyeColor.values()) {
                ItemStack stackColored = stack.copy();
                item.setColor(stackColored, ColorUtils.toIntColor(color));
                stacks.add(stackColored);
            }
        }

        ingredients.setOutputLists(ItemStack.class, Collections.singletonList(stacks));
    }
}
