package pl.asie.charset.lib.modcompat.jei;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.lib.loader.AnnotatedPluginHandler;
import pl.asie.charset.lib.recipe.DyeableItemRecipeFactory;
import pl.asie.charset.lib.recipe.InventoryCraftingIterator;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nonnull;
import java.util.Collections;

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

        for (IRecipe recipe : GameRegistry.findRegistry(IRecipe.class)) {
            if (recipe instanceof RecipeCharset) {
                InventoryCraftingIterator iterator = new InventoryCraftingIterator((RecipeCharset) recipe, false);
                while (iterator.hasNext()) {
                    iterator.next();
                    if (recipe.matches(iterator, null)) {
                        registry.addRecipes(Collections.singletonList(iterator.contain()), VanillaRecipeCategoryUid.CRAFTING);
                    }
                }
            }
        }

        // registry.handleRecipes(RecipeCharset.class, JEIRecipeCharset::create, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(InventoryCraftingIterator.Container.class, JEIRecipeContainer::create, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(DyeableItemRecipeFactory.Recipe.class, JEIRecipeDyeableItem::create, VanillaRecipeCategoryUid.CRAFTING);

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
