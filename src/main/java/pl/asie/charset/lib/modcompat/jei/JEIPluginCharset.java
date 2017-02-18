package pl.asie.charset.lib.modcompat.jei;

import com.google.common.collect.ImmutableSet;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.annotation.AnnotatedPluginHandler;
import pl.asie.charset.lib.annotation.AnnotationHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

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

        registry.addRecipeHandlers(new JEIRecipeCharset.Handler());

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
