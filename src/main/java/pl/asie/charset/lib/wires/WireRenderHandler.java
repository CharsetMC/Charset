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

package pl.asie.charset.lib.wires;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class WireRenderHandler {
	public enum TextureType {
		TOP,
		SIDE,
		EDGE,
		PARTICLE
	}

	protected final WireProvider provider;

	public WireRenderHandler(WireProvider provider) {
		this.provider = provider;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@SideOnly(Side.CLIENT)
	public abstract boolean isTranslucent();

	@SideOnly(Side.CLIENT)
	public float getWidth() {
		return provider.getWidth();
	}

	@SideOnly(Side.CLIENT)
	public float getHeight() {
		return provider.getHeight();
	}

	@SideOnly(Side.CLIENT)
	public abstract void refresh(TextureMap map);

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getTexture(TextureType type, Wire wire, EnumFacing facing, int connMask);

	@SideOnly(Side.CLIENT)
	public abstract int getColor(TextureType type, Wire wire, @Nullable EnumFacing direction);

	@SideOnly(Side.CLIENT)
	public boolean isTopSimple() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public boolean isDynamic() {
		return false;
	}
}
