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

package pl.asie.charset.module.power.electric;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.wires.*;
import pl.asie.simplelogic.wires.logic.PartWireBundled;

import javax.annotation.Nullable;

public class WireElectricOverlay extends WireRenderHandler implements IWireRenderContainer {
	private static final float EPSILON = 1/1024f;
	private final WireRenderHandler defaultHandler;
	private TextureAtlasSprite top;

	public WireElectricOverlay(WireProvider provider) {
		super(provider);
		this.defaultHandler = new WireRenderHandlerDefault(provider);
	}

	@Override
	public boolean isTranslucent() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getWidth() {
		return provider.getWidth() - (4f/16f);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getHeight() {
		return provider.getHeight() - EPSILON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void refresh(TextureMap map) {
		top = map.registerSprite(new ResourceLocation(provider.getTexturePrefix().toString() + "_overlay"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTexture(TextureType type, Wire wire, EnumFacing facing, int connMask) {
		return (type == TextureType.TOP && getColor(type, wire, facing) != 0) ? top : null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColor(TextureType type, Wire wire, @Nullable EnumFacing direction) {
		boolean lit = false;
		WireElectric we = (WireElectric) wire;

		if (direction != null) {
			lit = we.STORAGE[direction.ordinal()] != null && we.STORAGE[direction.ordinal()].isLit();
		} else {
			for (EnumFacing d : EnumFacing.VALUES) {
				if (we.STORAGE[d.ordinal()] != null) {
					lit = we.STORAGE[d.ordinal()] != null && we.STORAGE[d.ordinal()].isLit();
				}
				if (lit) {
					break;
				}
			}
		}

		return lit ? 0xFF6FEDFF : 0xFF3D5159;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getLightLevel(TextureType type, Wire wire, @Nullable EnumFacing direction) {
		boolean lit = false;
		WireElectric we = (WireElectric) wire;

		if (direction != null) {
			lit = we.STORAGE[direction.ordinal()] != null && we.STORAGE[direction.ordinal()].isLit();
		} else {
			for (EnumFacing d : EnumFacing.VALUES) {
				if (we.STORAGE[d.ordinal()] != null) {
					lit = we.STORAGE[d.ordinal()] != null && we.STORAGE[d.ordinal()].isLit();
				}
				if (lit) {
					break;
				}
			}
		}

		return lit ? 15 : 0;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public boolean isTopSimple() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isDynamic() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getLayerCount() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public WireRenderHandler get(int layer) {
		return layer == 1 ? this : defaultHandler;
	}
}
