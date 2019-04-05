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

package pl.asie.charset.module.storage.chests;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nullable;

public class ModelChestCharset extends WrappedBakedModel implements IStateParticleBakedModel {
	public ModelChestCharset(IBakedModel parent) {
		super(parent, ModelLoader.defaultTextureGetter().apply(new ResourceLocation("minecraft:blocks/planks_oak")));
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IBlockState state, @Nullable EnumFacing facing) {
		if (state instanceof IExtendedBlockState) {
			ItemMaterial material = ((IExtendedBlockState) state).getValue(BlockChestCharset.MATERIAL_PROP);
			if (material != null) {
				return RenderUtils.getItemSprite(material.getStack());
			}
		}

		return getParticleTexture();
	}
}
