package pl.asie.charset.lib.recipe;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;

import javax.annotation.Nonnull;

@JEIPlugin
public class JEIPluginCharsetLib implements IModPlugin {
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
        registry.addRecipeHandlers(new JEIRecipeCharset.Handler());
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {

    }
}
