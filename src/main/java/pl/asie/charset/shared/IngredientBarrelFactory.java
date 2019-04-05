/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.shared;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;
import pl.asie.charset.lib.utils.MethodHandleHelper;
import pl.asie.charset.module.storage.barrels.IngredientBarrel;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;

public class IngredientBarrelFactory implements IIngredientFactory {
    private MethodHandle BARREL_CREATE;

    @Nonnull
    @Override
    public Ingredient parse(JsonContext context, JsonObject json) {
        if (BARREL_CREATE == null) {
            BARREL_CREATE = MethodHandleHelper.findConstructor("pl.asie.charset.module.storage.barrels.IngredientBarrel", JsonContext.class, JsonObject.class);
        }

        try {
            return IngredientCharset.wrap((IngredientCharset) BARREL_CREATE.invoke(context, json));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
