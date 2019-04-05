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

package pl.asie.charset.lib.material;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.concurrent.TimeUnit;

public class ColorLookupHandler {
    public static final boolean DISABLE_CACHES = ModCharset.INDEV;
    public static final ColorLookupHandler INSTANCE = new ColorLookupHandler();
    private final Cache<Key, Integer> COLOR_MAP = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();
    private final TIntIntMap DEFAULT_COLOR_MAP = new TIntIntHashMap();

    private ColorLookupHandler() {
        DEFAULT_COLOR_MAP.put(OreDictionary.getOreID("logWood"), 0xff735e39);
    }

    public static final class Key {
        public final RenderUtils.AveragingMode averagingMode;
        public final ItemStack stack;
        private final int hash;

        public Key(ItemStack stack, RenderUtils.AveragingMode mode) {
            this.stack = stack;
            this.averagingMode = mode;
            this.hash =
                    Item.getIdFromItem(stack.getItem()) * 57
                    + stack.getMetadata() * 17
                    + (stack.hasTagCompound() ? stack.getTagCompound().hashCode() * 3 : 0)
                    + mode.ordinal();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) {
                return false;
            }

            Key k = (Key) o;
            if (k.averagingMode != averagingMode || !ItemUtils.equals(stack, k.stack, false, k.stack.getHasSubtypes(), true)) {
                return false;
            }

            return true;
        }
    }

    public void clear() {
        COLOR_MAP.invalidateAll();
    }

    public int getDefaultColor(ItemStack stack) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        for (int o : oreIDs) {
            if (DEFAULT_COLOR_MAP.containsKey(o))
                return DEFAULT_COLOR_MAP.get(o);
        }

        IBlockState state = ItemUtils.getBlockState(stack);
        return state.getMaterial().getMaterialMapColor().colorValue | 0xFF000000;
    }

    public int getColor(ItemStack stack, RenderUtils.AveragingMode mode) {
        Key key = new Key(stack, mode);
        Integer result = COLOR_MAP.getIfPresent(key);
        if (DISABLE_CACHES || result == null) {
            TextureAtlasSprite sprite = RenderUtils.getItemSprite(stack);
            int out;
            if (sprite.getIconName().endsWith("missingno")) {
                out = getDefaultColor(stack);
            } else {
                out = RenderUtils.getAverageColor(sprite, mode);
                int tintColor = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, 0);
                if (tintColor != -1) {
                    out = RenderUtils.multiplyColor(out, tintColor);
                }
            }
            COLOR_MAP.put(key, out);
            return out;
        } else {
            return result;
        }
    }
}
