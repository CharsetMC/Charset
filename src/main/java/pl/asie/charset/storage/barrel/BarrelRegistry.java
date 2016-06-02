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

package pl.asie.charset.storage.barrel;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BarrelRegistry {
    public static final BarrelRegistry INSTANCE = new BarrelRegistry();
    private final ArrayList<ItemStack> BARRELS = new ArrayList<>();

    private static boolean verifyRecipePart(Object o) {
        return o instanceof ItemStack || o instanceof Block || (o instanceof String && OreDictionary.doesOreNameExist((String) o));
    }

    private static ItemStack getRecipeStack(Object o) {
        if (o instanceof ItemStack) {
            return (ItemStack) o;
        } else if (o instanceof Block) {
            return new ItemStack((Block) o);
        } else if (o instanceof String) {
            List<ItemStack> stacks = OreDictionary.getOres((String) o, false);
            return stacks.size() > 0 ? stacks.get(0) : null;
        } else {
            return null;
        }
    }

    public void registerCraftable(Object log, Object slab) {
        if (!verifyRecipePart(log) || !verifyRecipePart(slab)) {
            return;
        }

        ItemStack logStack = getRecipeStack(log);
        ItemStack slabStack = getRecipeStack(slab);

        ItemStack barrel = register(TileEntityDayBarrel.Type.NORMAL, logStack, slabStack);
        GameRegistry.addRecipe(new ShapedOreRecipe(barrel,
                "W-W",
                "W W",
                "WWW",
                'W', log, '-', slab));
    }

    public ItemStack register(TileEntityDayBarrel.Type type, ItemStack log, ItemStack slab) {
        ItemStack ret = TileEntityDayBarrel.makeBarrel(type, log, slab);
        BARRELS.add(ret);
        return ret;
    }

    public Collection<ItemStack> getBarrels() {
        return Collections.unmodifiableList(BARRELS);
    }
}
