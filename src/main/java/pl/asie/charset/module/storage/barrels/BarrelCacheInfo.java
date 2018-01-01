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

package pl.asie.charset.module.storage.barrels;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.Set;

class BarrelCacheInfo implements IRenderComparable<BarrelCacheInfo> {
    final TextureAtlasSprite log, plank;
    final Set<BarrelUpgrade> upgrades;
    final Orientation orientation;
    final boolean isMetal;

    // Used as a cache field only
    final transient ItemStack logStack;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BarrelCacheInfo cacheInfo = (BarrelCacheInfo) o;

        if (isMetal != cacheInfo.isMetal) return false;
        if (!log.equals(cacheInfo.log)) return false;
        if (!plank.equals(cacheInfo.plank)) return false;
        if (!upgrades.equals(cacheInfo.upgrades)) return false;
        return orientation == cacheInfo.orientation;
    }

    @Override
    public int hashCode() {
        // log & plank don't have hashCode(), but they're unique.
        int result = log.hashCode();
        result = 31 * result + plank.hashCode();
        // result = 31 * result + upgrades.hashCode();
        result = 31 * result + orientation.hashCode();
        result = 31 * result + (isMetal ? 1 : 0);
        return result;
    }

    private BarrelCacheInfo(TextureAtlasSprite log, ItemStack logStack, TextureAtlasSprite plank, Set<BarrelUpgrade> upgrade, Orientation orientation, boolean isMetal) {
        this.log = log;
        this.logStack = logStack;
        this.plank = plank;
        this.upgrades = upgrade;
        this.orientation = orientation;
        this.isMetal = isMetal;
    }

    public static BarrelCacheInfo from(TileEntityDayBarrel barrel) {
        TextureAtlasSprite log = RenderUtils.getItemSprite(barrel.woodLog.getStack());
        TextureAtlasSprite slab = RenderUtils.getItemSprite(barrel.woodSlab.getStack());
        return new BarrelCacheInfo(log, barrel.woodLog.getStack(), slab, barrel.upgrades, barrel.orientation, isMetal(barrel.woodLog.getStack()));
    }

    public static BarrelCacheInfo from(ItemStack is) {
        TileEntityDayBarrel barrel = new TileEntityDayBarrel();
        barrel.loadFromStack(is);
        barrel.orientation = Orientation.FACE_NORTH_POINT_UP;
        return BarrelCacheInfo.from(barrel);
    }

    static boolean isMetal(ItemStack it) {
        if (it == null) return true;
        return !ItemMaterialRegistry.INSTANCE.matches(it, "wood");
    }

    @Override
    public boolean renderEquals(BarrelCacheInfo other) {
        return equals(other);
    }

    @Override
    public int renderHashCode() {
        return hashCode();
    }
}
