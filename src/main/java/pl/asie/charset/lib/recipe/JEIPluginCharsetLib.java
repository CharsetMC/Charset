package pl.asie.charset.lib.recipe;

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
    public static IItemBlacklist ITEM_BLACKLISTS;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        // TODO
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        // TODO
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        STACKS = registry.getJeiHelpers().getStackHelper();
        GUIS = registry.getJeiHelpers().getGuiHelper();
        RECIPE_TRANSFER_HANDLERS = registry.getJeiHelpers().recipeTransferHandlerHelper();
        ITEM_BLACKLISTS = registry.getJeiHelpers().getItemBlacklist();

        registry.addRecipeHandlers(new JEIRecipeCharset.Handler());
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {

    }
}
