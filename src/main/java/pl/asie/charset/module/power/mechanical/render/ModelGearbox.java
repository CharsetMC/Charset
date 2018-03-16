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

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.module.power.mechanical.BlockGearbox;

import javax.annotation.Nullable;

public class ModelGearbox extends ModelFactory<GearboxCacheInfo> {
	private static final ModelRotation[] ROTATIONS = new ModelRotation[] {
			ModelRotation.X180_Y0,
			ModelRotation.X0_Y0,
			ModelRotation.X90_Y0,
			ModelRotation.X90_Y180,
			ModelRotation.X90_Y270,
			ModelRotation.X90_Y90
	};

	public static final ModelGearbox INSTANCE = new ModelGearbox();
	public IModel model;

	protected ModelGearbox() {
		super(BlockGearbox.PROPERTY, new ResourceLocation("minecraft:blocks/planks_oak"));
	}

	@Override
	public IBakedModel bake(GearboxCacheInfo object, boolean isItem, BlockRenderLayer layer) {
		return model.uvlock(true).retexture(ImmutableMap.of(
				"#plank", object.plank.getIconName(),
				"plank", object.plank.getIconName()
		)).bake(ROTATIONS[object.facing.ordinal()], DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IBlockState state, @Nullable EnumFacing facing) {
		GearboxCacheInfo info = get(state);
		if (info != null) {
			return info.plank;
		} else {
			return super.getParticleTexture();
		}
	}

	@Override
	public GearboxCacheInfo fromItemStack(ItemStack stack) {
		return GearboxCacheInfo.from(stack);
	}
}
