/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.storage.barrels;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.material.ItemMaterial;

import javax.annotation.Nullable;
import java.util.*;

public final class BarrelRegistry {
    public static final BarrelRegistry INSTANCE = new BarrelRegistry();
    private final ArrayList<ItemStack> BARRELS = new ArrayList<>();
    private final EnumMap<TileEntityDayBarrel.Type, ArrayList<ItemStack>> BARRELS_TYPE = new EnumMap<>(TileEntityDayBarrel.Type.class);

    private BarrelRegistry() {
        for (TileEntityDayBarrel.Type type : TileEntityDayBarrel.Type.VALUES) {
            BARRELS_TYPE.put(type, new ArrayList<>());
        }
    }

    public void registerCraftable(ItemMaterial log, ItemMaterial slab) {
        for (TileEntityDayBarrel.Type type : TileEntityDayBarrel.Type.VALUES) {
            if (type == TileEntityDayBarrel.Type.CREATIVE) continue;
            register(type, log, slab);
        }
    }

    public @Nullable ItemStack register(TileEntityDayBarrel.Type type, ItemMaterial log, ItemMaterial slab) {
        if (!CharsetStorageBarrels.isEnabled(type)) return null;

        ItemStack ret = TileEntityDayBarrel.makeBarrel(type, log, slab);
        BARRELS.add(ret);
        BARRELS_TYPE.get(type).add(ret);

        return ret;
    }

    public List<ItemStack> getBarrels() {
        return Collections.unmodifiableList(BARRELS);
    }

    public List<ItemStack> getBarrels(TileEntityDayBarrel.Type type) {
        return Collections.unmodifiableList(BARRELS_TYPE.get(type));
    }
}
