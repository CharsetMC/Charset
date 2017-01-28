package pl.asie.charset.lib.modcompat.jei;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import javax.annotation.Nonnull;

@JEIPlugin
public class JEIPluginCharsetLib implements IModPlugin {
    public static IStackHelper STACKS;
    public static IRecipeTransferHandlerHelper RECIPE_TRANSFER_HANDLERS;
    public static IGuiHelper GUIS;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        STACKS = registry.getJeiHelpers().getStackHelper();
        GUIS = registry.getJeiHelpers().getGuiHelper();
        RECIPE_TRANSFER_HANDLERS = registry.getJeiHelpers().recipeTransferHandlerHelper();

        registry.addRecipeHandlers(new JEIRecipeCharset.Handler());
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {

    }
}
