/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.misc.scaffold;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.RenderUtils;

class ScaffoldCacheInfo implements IRenderComparable<ScaffoldCacheInfo> {
	public final TextureAtlasSprite plank;

	private ScaffoldCacheInfo(TextureAtlasSprite plank) {
		this.plank = plank;
	}

	public static ScaffoldCacheInfo from(TileScaffold tile) {
		return new ScaffoldCacheInfo(RenderUtils.getItemSprite(tile.getPlank().getStack()));
	}

	public static ScaffoldCacheInfo from(ItemStack stack) {
		TileScaffold tileScaffold = new TileScaffold();
		tileScaffold.loadFromStack(stack);
		return from(tileScaffold);
	}

	@Override
	public boolean renderEquals(ScaffoldCacheInfo other) {
		return other.plank == plank;
	}

	@Override
	public int renderHashCode() {
		return plank.hashCode();
	}
}
