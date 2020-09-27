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

package pl.asie.simplelogic.wires.logic;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.render.sprite.SpritesheetFactory;
import pl.asie.charset.lib.wires.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WireRenderHandlerOverlay extends WireRenderHandler implements IWireRenderContainer {
	public static class Color extends WireRenderHandler {
		@Nonnull
		private TextureAtlasSprite[] top;

		public Color(WireProvider provider) {
			super(provider);
		}

		@Override
		public boolean isTranslucent() {
			return false;
		}

		@Override
		public void refresh(TextureMap map) {
			top = SpritesheetFactory.register(map, new ResourceLocation("simplelogic", "blocks/wire/wire_bundled_colored_top"), 4, 4);
		}

		@Override
		public TextureAtlasSprite getTexture(TextureType type, Wire wire, EnumFacing facing, int connMask) {
			if (type != TextureType.TOP) return null;

			return top[connMask & 15];
		}

		@Override
		public int getColor(TextureType type, Wire wire, @Nullable EnumFacing direction) {
			return wire instanceof PartWireBundled ? ((PartWireBundled) wire).getBundledRenderColor() : -1;
		}
	}

	private static final float EPSILON = 1/1024f;
	private final WireRenderHandler defaultHandler;
	private final WireRenderHandler colorHandler;
	private TextureAtlasSprite top;

	public WireRenderHandlerOverlay(WireProvider provider, boolean isColored) {
		super(provider);
		this.defaultHandler = new WireRenderHandlerDefault(provider);
		this.colorHandler = isColored ? new Color(provider) : null;
	}

	@Override
	public boolean isTranslucent() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getWidth() {
		return provider.getWidth() + EPSILON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getHeight() {
		return provider.getHeight() + EPSILON;
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
		if (direction == null || !(wire instanceof PartWireBundled) || wire.getLocation() == WireFace.CENTER /* TODO */) return 0;

		int[] cache = ((PartWireBundled) wire).getInsulatedColorCache();
		if (cache == null) {
			return 0;
		}

		int v = cache[direction.ordinal()];
		if (v < 0) {
			return 0;
		}

		int c = 0xFF000000 | EnumDyeColor.byMetadata(v).getColorValue();
		return (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c << 16) & 0xFF0000);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isTopSimple() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getLayerCount() {
		return colorHandler != null ? 3 : 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public WireRenderHandler get(int layer) {
		if (colorHandler != null && layer == 1) {
			return colorHandler;
		}
		return layer > 0 ? this : defaultHandler;
	}
}
