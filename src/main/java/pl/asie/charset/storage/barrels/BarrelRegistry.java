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

package pl.asie.charset.storage.barrels;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.material.ItemMaterial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BarrelRegistry {
    public static final BarrelRegistry INSTANCE = new BarrelRegistry();
    private final ArrayList<ItemStack> BARRELS = new ArrayList<>();
    private final ArrayList<ItemStack> BARRELS_NORMAL = new ArrayList<>();

    public void registerCraftable(ItemMaterial log, ItemMaterial slab) {
        ItemStack barrel = register(TileEntityDayBarrel.Type.NORMAL, log, slab);
        GameRegistry.addRecipe(new ShapedOreRecipe(barrel,
                "W-W",
                "W W",
                "WWW",
                'W', log.getStack(), '-', slab.getStack()));
    }

    public ItemStack register(TileEntityDayBarrel.Type type, ItemMaterial log, ItemMaterial slab) {
        ItemStack ret = TileEntityDayBarrel.makeBarrel(type, log, slab);
        BARRELS.add(ret);
        if (type == TileEntityDayBarrel.Type.NORMAL)
            BARRELS_NORMAL.add(ret);
        return ret;
    }

    public Collection<ItemStack> getBarrels() {
        return Collections.unmodifiableList(BARRELS);
    }

    public Collection<ItemStack> getRegularBarrels() {
        return Collections.unmodifiableList(BARRELS_NORMAL);
    }
}
