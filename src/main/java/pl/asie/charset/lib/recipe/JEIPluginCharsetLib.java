package pl.asie.charset.lib.recipe;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

import javax.annotation.Nonnull;

@JEIPlugin
public class JEIPluginCharsetLib implements IModPlugin {
    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addRecipeHandlers(new JEIRecipeCharset.Handler());
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {

    }
}
