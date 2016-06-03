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

package pl.asie.charset.storage.crate;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.storage.barrel.TileEntityDayBarrel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CrateRegistry {
    public static final CrateRegistry INSTANCE = new CrateRegistry();
    private final ArrayList<ItemStack> CRATES = new ArrayList<>();

    private static boolean verifyRecipePart(Object o) {
        return o instanceof ItemStack || (o instanceof String && OreDictionary.doesOreNameExist((String) o));
    }

    private static ItemStack getRecipeStack(Object o) {
        if (o instanceof ItemStack) {
            return (ItemStack) o;
        } else if (o instanceof String) {
            List<ItemStack> stacks = OreDictionary.getOres((String) o, false);
            return stacks.size() > 0 ? stacks.get(0) : null;
        } else {
            return null;
        }
    }

    public void registerCraftable(Object plank, Object stick) {
        if (!verifyRecipePart(plank) || !verifyRecipePart(stick)) {
            return;
        }

        ItemStack plankStack = getRecipeStack(plank);

        ItemStack barrel = register(plankStack);
        GameRegistry.addRecipe(new ShapedOreRecipe(barrel,
                "sWs",
                "WsW",
                "sWs",
                'W', plank, 's', stick));
    }

    public ItemStack register(ItemStack plank) {
        ItemStack ret = TileEntityCrate.create(plank);
        CRATES.add(ret);
        return ret;
    }

    public Collection<ItemStack> getCrates() {
        return Collections.unmodifiableList(CRATES);
    }
}
