/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.power.mechanical.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.misc.shelf.BlockShelf;
import pl.asie.charset.module.misc.shelf.TileShelf;
import pl.asie.charset.module.power.mechanical.TileGearbox;

public class GearboxCacheInfo implements IRenderComparable<GearboxCacheInfo> {
	public final TextureAtlasSprite plank;
	public final EnumFacing facing;

	private GearboxCacheInfo(TextureAtlasSprite plank, EnumFacing facing) {
		this.plank = plank;
		this.facing = facing;
	}

	public static GearboxCacheInfo from(IBlockState state, TileGearbox tile) {
		return new GearboxCacheInfo(RenderUtils.getItemSprite(tile.getMaterial().getStack()), state.getValue(Properties.FACING));
	}

	public static GearboxCacheInfo from(ItemStack stack) {
		TileGearbox tile = new TileGearbox();
		tile.loadFromStack(stack);
		return new GearboxCacheInfo(RenderUtils.getItemSprite(tile.getMaterial().getStack()), EnumFacing.NORTH);
	}

	@Override
	public boolean renderEquals(GearboxCacheInfo other) {
		return other.plank == plank && other.facing == facing;
	}

	@Override
	public int renderHashCode() {
		return plank.hashCode() * 5 + facing.ordinal();
	}
}
