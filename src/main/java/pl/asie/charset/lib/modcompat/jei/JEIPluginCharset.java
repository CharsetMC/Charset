package pl.asie.charset.lib.modcompat.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import pl.asie.charset.lib.annotation.AnnotatedPluginHandler;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nonnull;

@JEIPlugin
public class JEIPluginCharset extends AnnotatedPluginHandler<IModPlugin> implements IModPlugin {
    public static IStackHelper STACKS;
    public static IRecipeTransferHandlerHelper RECIPE_TRANSFER_HANDLERS;
    public static IGuiHelper GUIS;

    public JEIPluginCharset() {
        super(CharsetJEIPlugin.class);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        for (IModPlugin plugin : getPlugins()) {
            plugin.registerItemSubtypes(subtypeRegistry);
        }
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        for (IModPlugin plugin : getPlugins()) {
            plugin.registerIngredients(registry);
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        STACKS = registry.getJeiHelpers().getStackHelper();
        GUIS = registry.getJeiHelpers().getGuiHelper();
        RECIPE_TRANSFER_HANDLERS = registry.getJeiHelpers().recipeTransferHandlerHelper();

        registry.handleRecipes(RecipeCharset.class, new JEIRecipeCharset.Factory(), VanillaRecipeCategoryUid.CRAFTING);

        for (IModPlugin plugin : getPlugins()) {
            plugin.register(registry);
        }
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        for (IModPlugin plugin : getPlugins()) {
            plugin.onRuntimeAvailable(jeiRuntime);
        }
    }
}
