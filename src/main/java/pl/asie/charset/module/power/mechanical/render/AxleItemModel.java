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

package pl.asie.charset.module.power.mechanical.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.mechanical.CharsetPowerMechanical;

import javax.annotation.Nullable;

public class AxleItemModel extends ModelFactory<AxleItemModel.Key> {
	public static class Key implements IRenderComparable<Key> {
		private final ItemMaterial material;

		public Key(ItemStack stack) {
			this.material = ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material", "plank");
		}

		@Override
		public boolean renderEquals(Key other) {
			return other.material == material;
		}

		@Override
		public int renderHashCode() {
			return material.hashCode();
		}
	}

	private final IBakedModel parent;

	public AxleItemModel(IBakedModel parent) {
		super(null, new ResourceLocation("minecraft:blocks/planks_oak"));
		this.parent = parent;
	}

	@Nullable
	@Override
	protected Key get(IBlockState state) {
		return new Key(ItemStack.EMPTY);
	}

	@Override
	public IBakedModel bake(Key object, boolean isItem, BlockRenderLayer layer) {
		TextureAtlasSprite replacementSprite = RenderUtils.getItemSprite(object.material.getStack());

		try {
			return ModelTransformer.transform(parent,
					CharsetPowerMechanical.blockAxle.getDefaultState(), 0L, (quad, element, data) -> {
						if (element.getUsage() == VertexFormatElement.EnumUsage.UV) {
							float u = quad.getSprite().getUnInterpolatedU(data[0]);
							float v = quad.getSprite().getUnInterpolatedV(data[1]);

							return new float[]{
									replacementSprite.getInterpolatedU(u),
									replacementSprite.getInterpolatedV(v),
									data[2],
									data[3]
							};
						} else {
							return data;
						}
					});
		} catch (ModelTransformer.TransformationFailedException e) {
			ModCharset.logger.warn("Baking axle item model for sprite " + replacementSprite.getIconName() + " failed!", e);
			return parent;
		}
	}

	@Override
	public Key fromItemStack(ItemStack stack) {
		return new Key(stack);
	}
}
