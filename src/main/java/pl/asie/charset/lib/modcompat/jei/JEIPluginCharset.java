/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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
