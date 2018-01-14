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

package pl.asie.charset.module.misc.shelf;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.vecmath.Vector3f;

public class ModelShelf extends ModelFactory<ShelfCacheInfo> {
	public static final ModelShelf INSTANCE = new ModelShelf();
	public static final TRSRTransformation STATE_BACK = new TRSRTransformation(
			new Vector3f(0, 0, 0.5F),
			null,
			null,
			null
	);
	public static IModel shelfModel;

	public ModelShelf() {
		super(TileShelf.PROPERTY, TextureMap.LOCATION_MISSING_TEXTURE);
	}

	@Override
	public IBakedModel bake(ShelfCacheInfo info, boolean isItem, BlockRenderLayer layer) {
		IModel retexturedModel = (shelfModel.retexture(ImmutableMap.of("plank", info.plank.getIconName()))).uvlock(false);
		IModelState state = ModelRotation.getModelRotation(0, (int) info.facing.getHorizontalAngle());
		if (info.back) {
			state = new ModelStateComposition(state, STATE_BACK);
		}
		return new WrappedBakedModel(retexturedModel.bake(state, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()), info.plank).addDefaultBlockTransforms();
	}

	@Override
	public ShelfCacheInfo fromItemStack(ItemStack stack) {
		return ShelfCacheInfo.from(stack);
	}
}
