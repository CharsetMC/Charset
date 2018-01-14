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

package pl.asie.charset.module.storage.barrels;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IOutputSupplierFactory;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

public class BarrelUpgradeOutputSupplier implements IOutputSupplier {
    private final BarrelUpgrade upgradeType;

    private BarrelUpgradeOutputSupplier(BarrelUpgrade upgradeType) {
        this.upgradeType = upgradeType;
    }

    @Override
    public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
        for (Ingredient i : matcher.getMatchedIngredients()) {
            ItemStack is = matcher.getStack(i);
            if (is.getItem() instanceof ItemDayBarrel || is.getItem() instanceof ItemMinecartDayBarrel) {
                is = is.copy();
                is.setCount(1);
                return TileEntityDayBarrel.addUpgrade(is, upgradeType);
            }
        }

        return null;
    }

    @Override
    public ItemStack getDefaultOutput() {
        return new ItemStack(CharsetStorageBarrels.barrelCartItem);
    }

    public static class Factory implements IOutputSupplierFactory {
        @Override
        public IOutputSupplier parse(JsonContext context, JsonObject json) {
            return new BarrelUpgradeOutputSupplier(BarrelUpgrade.valueOf(JsonUtils.getString(json, "upgrade")));
        }
    }
}
