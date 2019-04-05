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

package pl.asie.charset.lib.wires;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.render.sprite.SpritesheetFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WireRenderHandlerDefault extends WireRenderHandler {
	private final String domain, path;

	@Nonnull private TextureAtlasSprite[] top;
	@Nullable private TextureAtlasSprite side;
	@Nullable private TextureAtlasSprite edge;
	@Nullable private TextureAtlasSprite corner;

	public WireRenderHandlerDefault(WireProvider provider) {
		super(provider);

		ResourceLocation location = provider.getTexturePrefix();
		this.domain = location.getNamespace();
		if (!location.getPath().endsWith("/")) {
			this.path = location.getPath() + "_";
		} else {
			this.path = location.getPath();
		}
	}

	@Override
	public boolean isTranslucent() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void refresh(TextureMap map) {
		top = SpritesheetFactory.register(map, new ResourceLocation(domain, path + "top"), 4, 4);
		if (!provider.isFlat()) {
			edge = map.registerSprite(new ResourceLocation(domain, path + "edge"));
			side = map.registerSprite(new ResourceLocation(domain, path + "side"));
			corner = map.registerSprite(new ResourceLocation(domain, path + "corner"));
		} else {
			edge = null;
			side = null;
			corner = null;
		}
	}

	@Override
	public TextureAtlasSprite getTexture(TextureType type, Wire wire, @Nullable EnumFacing facing, int connMask) {
		switch (type) {
			case TOP:
				return top[connMask & 15];
			case SIDE:
				return side;
			case EDGE:
				return edge;
			case CORNER:
				return corner;
			default:
				return top[15];
		}
	}

	@Override
	public int getColor(TextureType type, Wire wire, @Nullable EnumFacing direction) {
		return wire.getRenderColor();
	}
}
