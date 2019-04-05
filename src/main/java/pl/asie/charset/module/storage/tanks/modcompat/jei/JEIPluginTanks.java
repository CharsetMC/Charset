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

package pl.asie.charset.module.storage.tanks.modcompat.jei;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;
import pl.asie.charset.module.storage.tanks.CharsetStorageTanks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@CharsetJEIPlugin("storage.tanks")
public class JEIPluginTanks implements IModPlugin {
    private static final ISubtypeRegistry.ISubtypeInterpreter interpreter = new ISubtypeRegistry.ISubtypeInterpreter() {
        @Nullable
        @Override
        public String apply(ItemStack itemStack) {
            return "charsetGlassTank:" + (itemStack.hasTagCompound() ? itemStack.getTagCompound().getInteger("color") : "-1");
        }
    };

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(CharsetStorageTanks.tankItem, interpreter);
    }
}
