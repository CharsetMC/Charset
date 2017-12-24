/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.storage.barrels.modcompat.chiselsandbits;

import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.modcompat.chiselsandbits.CharsetChiselsAndBitsPlugin;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;

import java.lang.reflect.Field;

@CharsetChiselsAndBitsPlugin("storage.barrels")
public class CharsetChiselsBitsBarrelCompat implements IChiselsAndBitsAddon {
    private static Item cb_block_bit, cb_bit_bag;

    private int getBitBagStackMultiplier(IChiselAndBitsAPI api) {
        int bitStackSize = new ItemStack(cb_block_bit).getMaxStackSize();

        // Idea 1: Query the bit bag, if present.
        if (cb_bit_bag != null) {
            IBitBag bag = api.getBitbag(new ItemStack(cb_bit_bag));
            if (bag != null) {
                return bag.getBitbagStackSize() / bitStackSize;
            }
        }

        // Idea 2: Reflect into the mod. (Sorry, Algo.)
        try {
            Class coreClass = Class.forName("mod.chiselsandbits.core.ChiselsAndBits");
            if (coreClass != null) {
                Object config = coreClass.getMethod("getConfig").invoke(null);
                Field bagStackSizeField = config.getClass().getField("bagStackSize");
                bagStackSizeField.setAccessible(true);
                int v = (int) bagStackSizeField.get(config);
                return v / bitStackSize;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // Idea the final: Use hardcoded value.
        ModCharset.logger.warn("Chisels and Bits present but could not read bag stack size! Using default (x8).");
        return 8;
    }

    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api) {
        cb_block_bit = Item.getByNameOrId("chiselsandbits:block_bit");
        if (cb_block_bit != null) {
            cb_bit_bag = Item.getByNameOrId("chiselsandbits:bit_bag");
            int multiplier = getBitBagStackMultiplier(api);
            CharsetStorageBarrels.stackDivisorMultiplierMap.put(cb_block_bit, multiplier);
            CharsetStorageBarrels.stackSizeMultiplierMap.put(cb_block_bit, multiplier);
        }
    }
}
