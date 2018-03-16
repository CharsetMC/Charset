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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.CharsetPower;
import pl.asie.charset.module.power.mechanical.TileAxle;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TileAxleRenderer extends FastTESR<TileAxle> {
	public static final TileAxleRenderer INSTANCE = new TileAxleRenderer();
	protected static BlockModelRenderer renderer;

	private static final int ACCURACY = 48;
	private static final String[] TAGS = new String[] { "axis=x", "axis=y", "axis=z" };

	private static class Key {
		private final ItemMaterial material;
		private final int axis, rotation, texturing;
		private final int position;
		private final int hash;

		public Key(ItemMaterial material, int axis, int rotation, long texturing, float position) {
			this.material = material;
			this.axis = axis;
			this.rotation = rotation;
			this.texturing = (int) (texturing & 7);
			int tPos = (int) ((position * ACCURACY / 90f)) % ACCURACY;
			if (tPos < 0) tPos += ACCURACY;
			this.position = tPos;
			this.hash = (this.axis * 13 + this.rotation * 7 + this.texturing * 3 + this.position) * 31 + material.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Key)) {
				return false;
			} else {
				Key o = (Key) other;
				return o.material == material && o.axis == axis && o.rotation == rotation && o.texturing == texturing && o.position == position && o.hash == hash;
			}
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	private final IBakedModel[] bakedModels = new IBakedModel[3];
	private final Cache<Key, IBakedModel> renderCache = CacheBuilder.newBuilder()
			.softValues()
			.concurrencyLevel(1)
			.expireAfterWrite(1, TimeUnit.MINUTES)
			.build();

	private TileAxleRenderer() {

	}

	private IBakedModel build(Key key) {
		TextureAtlasSprite replacementSprite = RenderUtils.getItemSprite(key.material.getStack());
		float factor = (float) (key.position * Math.PI / (ACCURACY * 2));

		return ModelTransformer.transform(bakedModels[key.axis],
				CharsetPower.blockAxle.getDefaultState(), 0L, (quad, element, data) -> {
					float factorf = 0.0f;

					if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
						factorf = 0.5f;
					} else if (element.getUsage() == VertexFormatElement.EnumUsage.UV) {
						float u = quad.getSprite().getUnInterpolatedU(data[0]);
						float v = (quad.getSprite().getUnInterpolatedV(data[1]) % 4) + (4 * (key.texturing & 3));
						if (key.texturing >= 4) {
							u = 16 - u;
						}

						return new float[] {
								replacementSprite.getInterpolatedU(u),
								replacementSprite.getInterpolatedV(v),
								data[2],
								data[3]
						};
					} else if (element.getUsage() != VertexFormatElement.EnumUsage.NORMAL) {
						return data;
					}

					if (factor <= 1e-5f) {
						return data;
					}

					float x = data[0] - factorf;
					float y = data[1] - factorf;
					float z = data[2] - factorf;

					switch (key.rotation) {
						case 0:
							return new float[] {
									data[0],
									y * MathHelper.cos(factor) - z * MathHelper.sin(factor) + factorf,
									y * MathHelper.sin(factor) + z * MathHelper.cos(factor) + factorf,
									data[3]
							};
						case 1:
						default:
							return new float[] {
									z * MathHelper.sin(factor) + x * MathHelper.cos(factor) + factorf,
									data[1],
									z * MathHelper.cos(factor) - x * MathHelper.sin(factor) + factorf,
									data[3]
							};
						case 2:
							return new float[] {
									x * MathHelper.cos(factor) - y * MathHelper.sin(factor) + factorf,
									x * MathHelper.sin(factor) + y * MathHelper.cos(factor) + factorf,
									data[2],
									data[3]
							};
					}
				});

	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		for (int i = 0; i < 3; i++) {
			bakedModels[i] = event.getModelRegistry().getObject(new ModelResourceLocation("charset:axle", TAGS[i]));
		}

		ModelResourceLocation invLoc = new ModelResourceLocation("charset:axle", "inventory");
		event.getModelRegistry().putObject(invLoc, new AxleItemModel(event.getModelRegistry().getObject(invLoc)));
	}

	@Override
	public void renderTileEntityFast(TileAxle te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		float rotation = te.getWorld().getTotalWorldTime() + partialTicks;
		rotation *= te.rotSpeedClient*4.5f;

		/* boolean direction = EnumFacing.SOUTH.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
		if (direction) {
			rotation = 0 - rotation;
		} */

		BlockPos pos = te.getPos();
		IBlockState state = te.getWorld().getBlockState(te.getPos());
		int axis = state.getValue(Properties.AXIS).ordinal();

		Key key = new Key(
				te.getMaterial(),
				axis, axis,
				MathHelper.getPositionRandom(pos),
				rotation
		);

		buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
		try {
			renderer.renderModel(getWorld(), renderCache.get(key, () -> build(key)), state, pos, buffer, false);
		} catch (ExecutionException e) {
			ModCharset.logger.error("Axle rendering error!", e);
		}
	}
}
