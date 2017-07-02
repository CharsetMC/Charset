/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.Set;

class BarrelCacheInfo implements IRenderComparable<BarrelCacheInfo> {
    final TextureAtlasSprite log, plank;
    final Set<TileEntityDayBarrel.Upgrade> upgrades;
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

    private BarrelCacheInfo(TextureAtlasSprite log, ItemStack logStack, TextureAtlasSprite plank, Set<TileEntityDayBarrel.Upgrade> upgrade, Orientation orientation, boolean isMetal) {
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
