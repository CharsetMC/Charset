/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.pipes.pipe;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.multipart.MultipartSpecialRendererBase;
import pl.asie.charset.lib.render.ModelTransformer;
import pl.asie.charset.lib.render.SimpleBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

public class SpecialRendererPipe extends MultipartSpecialRendererBase<PartPipe> {
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();
	private static final float ITEM_RANDOM_OFFSET = 0.01F;

	private static final float TANK_MIN = 4.01f;
	private static final float TANK_MAX = 11.99f;
	private static final Vector3f[] TANK_FROM = new Vector3f[] {
			new Vector3f(TANK_MIN, 0, TANK_MIN),
			new Vector3f(TANK_MIN, TANK_MAX, TANK_MIN),
			new Vector3f(TANK_MIN, TANK_MIN, 0),
			new Vector3f(TANK_MIN, TANK_MIN, TANK_MAX),
			new Vector3f(0, TANK_MIN, TANK_MIN),
			new Vector3f(TANK_MAX, TANK_MIN, TANK_MIN),
			new Vector3f(TANK_MIN, TANK_MIN, TANK_MIN)
	};
	private final Vector3f[] TANK_TO = new Vector3f[] {
			new Vector3f(TANK_MAX, TANK_MIN, TANK_MAX),
			new Vector3f(TANK_MAX, 16, TANK_MAX),
			new Vector3f(TANK_MAX, TANK_MAX, TANK_MIN),
			new Vector3f(TANK_MAX, TANK_MAX, 16),
			new Vector3f(TANK_MIN, TANK_MAX, TANK_MAX),
			new Vector3f(16, TANK_MAX, TANK_MAX),
			new Vector3f(TANK_MAX, TANK_MAX, TANK_MAX)
	};

	private final BlockModelRenderer renderer = mc.getBlockRendererDispatcher().getBlockModelRenderer();
	private final RenderItem renderItem = mc.getRenderItem();
	private final ItemColors itemColors = mc.getItemColors();
	private final IBlockState DEFAULT_STATE = Blocks.AIR.getDefaultState();
	private final ItemModelTransformer ITEM_MODEL_TRANSFORMER = new ItemModelTransformer();

	private final class FluidColorTransformer implements ModelTransformer.IVertexTransformer {
		private final int color;

		public FluidColorTransformer(int color) {
			this.color = color;
		}

		@Override
		public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
			if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
				for (int i = 0; i < Math.min(data.length, 4); i++) {
					data[i] = data[i] * ((color >> (i * 8)) & 0xFF) / 255.0f;
				}
			}
			return data;
		}
	}

	private final class ItemModelTransformer implements ModelTransformer.IVertexTransformer {
		private ItemStack stack;
		private float[] offset;
		private float scale;
		private EnumFacing direction;

		@Override
		public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
			if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				if (direction != null) {
					float z = data[0];
					switch (direction) {
						case WEST:
							data[0] = 1.0f - data[0];
							data[2] = 1.0f - data[2];
							break;
						case NORTH:
							data[0] = data[2];
							data[2] = 1.0f - z;
							break;
						case SOUTH:
							data[0] = 1.0f - data[2];
							data[2] = z;
							break;
					}
				}
				for (int i = 0; i < 3; i++) {
					data[i] = ((data[i] - 0.5f) * scale) + offset[i];
				}
			} else if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR && quad.hasTintIndex()) {
				int k = itemColors.getColorFromItemstack(stack, quad.getTintIndex());
				if (EntityRenderer.anaglyphEnable) {
					k = TextureUtil.anaglyphColor(k);
				}

				for (int i = 0; i < Math.min(data.length, 3); i++) {
					data[i] = data[i] * ((k >> ((2 - i) * 8)) & 0xFF) / 255.0f;
				}
			}
			return data;
		}
	};

	private class FluidEntry {
		public final FluidStack stack;
		public final int side;
		public final int color;
		private final int hash;

		public FluidEntry(FluidStack stack, int color, EnumFacing side) {
			this.stack = stack;
			this.color = color;
			this.side = side != null ? side.ordinal() : 6;
			this.hash = Objects.hash(stack, color, side);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof FluidEntry)) {
				return false;
			}

			FluidEntry f = (FluidEntry) o;
			return f.stack.getFluid() == stack.getFluid() && f.stack.amount == stack.amount && f.color == color && f.side == side;
		}

		@Override
		public int hashCode() {
			return this.hash;
		}
	}

	private final Cache<ItemStack, IBakedModel> itemModelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
	private final Cache<FluidEntry, IBakedModel> fluidModelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	private IBakedModel renderFluidCube(IBlockAccess world, BlockPos pos, Vector3f from, Vector3f to, FluidEntry entry, VertexBuffer buffer) {
		TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
		TextureAtlasSprite sprite = map.getTextureExtry(entry.stack.getFluid().getStill().toString());
		if (sprite == null) {
			sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
			if (sprite == null) {
				return null;
			}
		}

		SimpleBakedModel smodel = new SimpleBakedModel();
		for (EnumFacing facing : EnumFacing.VALUES) {
			smodel.addQuad(null, RenderUtils.bakeFace(from, to, facing, sprite, -1));
		}

		if (entry.color == -1) {
			return smodel;
		} else {
			return ModelTransformer.transform(smodel, entry.stack.getFluid().getBlock().getDefaultState(), 0L, new FluidColorTransformer(entry.color));
		}
	}

	private final Set<PipeItem> SLOW_ITEMS = new HashSet<>();

	private float getItemScale(PipeItem item) {
		return item.stack.getItem() instanceof ItemBlock ? 0.35f : 0.4f;
	}

	private float[] calculateItemOffset(PipeItem item, float partialTicks) {
		EnumFacing id = item.getDirection();
		float[] offset;

		if (id == null) {
			return new float[] { 0.5f, 0.5f, 0.5f };
		} else if (partialTicks == 0 || item.isStuck() || (!item.hasReachedCenter() && item.getProgress() == 0.5F)) {
			offset = new float[] { item.getX(), item.getY(), item.getZ() };
		} else {
			float partialMul = partialTicks * PipeItem.SPEED / PipeItem.MAX_PROGRESS;
			offset = new float[]{
				item.getX() + (partialMul * id.getFrontOffsetX()),
				item.getY() + (partialMul * id.getFrontOffsetY()),
				item.getZ() + (partialMul * id.getFrontOffsetZ())
			};
		}

		PREDICTIVE_ITEM_RANDOM.setSeed(item.id);

		switch (id.getAxis()) {
			case Y:
			case X:
				offset[0] += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
				break;
			case Z:
				offset[2] += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
				break;
		}

		return offset;
	}

	@Override
	public void renderMultipartFast(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
		if (part == null) {
			return;
		}

		BlockPos pos = part.getPos();
		buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

		FluidStack fluidType = part.fluid.fluidStack;
		if (fluidType != null) {
			World world = part.getWorld();
			for (int i = 0; i < part.fluid.tanks.length; i++) {
				PipeFluidContainer.Tank tank = part.fluid.tanks[i];
				if (tank.amount > 0) {
					FluidStack stack = fluidType.copy();
					stack.amount = tank.amount;

					FluidEntry entry = new FluidEntry(stack, part.fluid.fluidColor, tank.location);
					IBakedModel model = fluidModelCache.getIfPresent(entry);
					if (model == null) {
						Vector3f from = TANK_FROM[i];
						Vector3f to = TANK_TO[i];
						to = new Vector3f(to.x, to.y + ((from.y - to.y) * (1f - ((float) tank.amount / tank.getCapacity()))), to.z);
						model = renderFluidCube(world, pos, from, to, entry, buffer);
						fluidModelCache.put(entry, model);
					}

					renderer.renderModelSmooth(world, model, entry.stack.getFluid().getBlock().getDefaultState(), pos, buffer, false, 0L);
				}
			}
		}

		synchronized (part.getPipeItems()) {
			for (PipeItem item : part.getPipeItems()) {
				EnumFacing id = item.getDirection();
				ItemStack stack = item.getStack();
				if (stack == null || stack.getItem() == null) {
					continue;
				}

				IBakedModel model = itemModelCache.getIfPresent(stack);
				if (model == null) {
					model = renderItem.getItemModelWithOverrides(stack, part.getWorld(), null);
					itemModelCache.put(stack, model);
				}

				if (model.isBuiltInRenderer()) {
					if (!part.renderFast) {
						SLOW_ITEMS.add(item);
					} else {
						part.renderFast = false;
					}
					continue;
				}

				ITEM_MODEL_TRANSFORMER.stack = stack;
				ITEM_MODEL_TRANSFORMER.scale = getItemScale(item);
				ITEM_MODEL_TRANSFORMER.direction = id;
				ITEM_MODEL_TRANSFORMER.offset = calculateItemOffset(item, partialTicks);

				renderer.renderModel(getWorld(), ModelTransformer.transform(model, DEFAULT_STATE, 0L, ITEM_MODEL_TRANSFORMER), DEFAULT_STATE, pos, buffer, false);
			}
		}

		buffer.setTranslation(0, 0, 0);
	}

	@Override
	public void renderMultipartAt(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage) {
		if (part == null) {
			return;
		}

		renderMultipartFastFromSlow(part, x, y, z, partialTicks, destroyStage);

		if (SLOW_ITEMS.size() > 0) {
			for (PipeItem item : SLOW_ITEMS) {
				EnumFacing id = item.getDirection();
				ItemStack stack = item.getStack();
				float scale = getItemScale(item);
				float[] offset = calculateItemOffset(item, partialTicks);

				GlStateManager.pushMatrix();

				IBakedModel model = itemModelCache.getIfPresent(stack);
				if (model == null) {
					model = renderItem.getItemModelWithOverrides(stack, part.getWorld(), null);
				}

				GlStateManager.translate(x + offset[0], y + offset[1], z + offset[2]);
				GlStateManager.scale(scale, scale, scale);
				if (id != null) {
					GlStateManager.rotate(270.0f - id.getHorizontalAngle(), 0, 1, 0);
				}

				renderItem.renderItem(stack, model);
				GlStateManager.popMatrix();
			}

			SLOW_ITEMS.clear();
		} else {
			part.renderFast = true;
		}
	}

	public void clearCache() {
		itemModelCache.invalidateAll();
		fluidModelCache.invalidateAll();
	}
}
