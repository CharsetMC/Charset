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

package pl.asie.charset.module.misc.shelf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.RenderUtils;

class ShelfCacheInfo implements IRenderComparable<ShelfCacheInfo> {
	public final TextureAtlasSprite plank;
	public final EnumFacing facing;
	public final boolean back;

	private ShelfCacheInfo(TextureAtlasSprite plank, EnumFacing facing, boolean back) {
		this.plank = plank;
		this.facing = facing;
		this.back = back;
	}

	public static ShelfCacheInfo from(IBlockState state, TileShelf tile) {
		return new ShelfCacheInfo(RenderUtils.getItemSprite(tile.getPlank().getStack()), state.getValue(Properties.FACING4), state.getValue(BlockShelf.BACK));
	}

	public static ShelfCacheInfo from(ItemStack stack) {
		TileShelf tile = new TileShelf();
		tile.loadFromStack(stack);
		return new ShelfCacheInfo(RenderUtils.getItemSprite(tile.getPlank().getStack()), EnumFacing.SOUTH, true);
	}

	@Override
	public boolean renderEquals(ShelfCacheInfo other) {
		return other.plank == plank && other.facing == facing && other.back == back;
	}

	@Override
	public int renderHashCode() {
		return (back ? 31 : 0) + plank.hashCode() * 5 + facing.ordinal();
	}
}
