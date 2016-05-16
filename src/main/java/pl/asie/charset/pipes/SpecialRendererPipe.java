package pl.asie.charset.pipes;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.lib.render.ModelTransformer;
import pl.asie.charset.lib.render.SimpleBakedModel;

public class SpecialRendererPipe extends MultipartSpecialRenderer<PartPipe> {
	protected static BlockModelRenderer renderer;
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();
	private static final float ITEM_RANDOM_OFFSET = 0.01F;
	private final ItemModelTransformer ITEM_MODEL_TRANSFORMER = new ItemModelTransformer();
	private final CharsetFaceBakery faceBakery = new CharsetFaceBakery();

	private final class FluidModelTransformer implements ModelTransformer.IVertexTransformer {
		private final int color;

		public FluidModelTransformer(int color) {
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
		private final float[] offset = new float[3];
		private boolean isBlock;

		@Override
		public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
			if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				for (int i = 0; i < 3; i++) {
					if (isBlock) {
						data[i] = (data[i] * 0.35f) + 0.075f + offset[i];
					} else {
						data[i] = (data[i] * 0.45f) + 0.025f + offset[i];
					}
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

	private final Cache<FluidEntry, IBakedModel> fluidModelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	private void renderFluidCube(IBlockAccess world, BlockPos pos, Vector3f from, Vector3f to, FluidEntry entry, VertexBuffer buffer) {
		IBakedModel model = fluidModelCache.getIfPresent(entry);
		if (model == null) {
			TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
			TextureAtlasSprite sprite = map.getTextureExtry(entry.stack.getFluid().getStill().toString());
			if (sprite == null) {
				sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
				if (sprite == null) {
					return;
				}
			}

			SimpleBakedModel smodel = new SimpleBakedModel();
			smodel.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, from.z),
					new Vector3f(to.x, from.y, to.z),
					-1, sprite, EnumFacing.DOWN, ModelRotation.X0_Y0, false
			));
			smodel.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, to.y, from.z),
					new Vector3f(to.x, to.y, to.z),
					-1, sprite, EnumFacing.UP, ModelRotation.X0_Y0, false
			));
			smodel.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, from.z),
					new Vector3f(from.x, to.y, to.z),
					-1, sprite, EnumFacing.WEST, ModelRotation.X0_Y0, false
			));
			smodel.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(to.x, from.y, from.z),
					new Vector3f(to.x, to.y, to.z),
					-1, sprite, EnumFacing.EAST, ModelRotation.X0_Y0, false
			));
			smodel.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, from.z),
					new Vector3f(to.x, to.y, from.z),
					-1, sprite, EnumFacing.NORTH, ModelRotation.X0_Y0, false
			));
			smodel.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, to.z),
					new Vector3f(to.x, to.y, to.z),
					-1, sprite, EnumFacing.SOUTH, ModelRotation.X0_Y0, false
			));
			model = smodel;

			if (entry.color == -1) {
				fluidModelCache.put(entry, model);
			} else {
				fluidModelCache.put(entry, ModelTransformer.transform(model, entry.stack.getFluid().getBlock().getDefaultState(), 0L, new FluidModelTransformer(entry.color)));
			}
		}

		renderer.renderModelSmooth(world, model, entry.stack.getFluid().getBlock().getDefaultState(), pos, buffer, false, 0L);
	}

	@Override
	public void renderMultipartFast(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
		if (part == null) {
			return;
		}

		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		BlockPos pos = part.getPos();

		buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

		if (part.fluid.fluidStack != null) {
			FluidStack base = part.fluid.fluidStack;
			for (PipeFluidContainer.Tank tank : part.fluid.tanks) {
				if (tank.amount > 0) {
					Vector3f from = new Vector3f(4.01f, 4.01f, 4.01f);
					Vector3f to = new Vector3f(11.99f, 11.99f, 11.99f);

					if (tank.location != null) {
						if (tank.location.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
							switch (tank.location.getAxis()) {
								case X:
									to.x = from.x;
									from.x = 0;
									break;
								case Y:
									to.y = from.y;
									from.y = 0;
									break;
								case Z:
									to.z = from.z;
									from.z = 0;
									break;
							}
						} else {
							switch (tank.location.getAxis()) {
								case X:
									from.x = to.x;
									to.x = 16;
									break;
								case Y:
									from.y = to.y;
									to.y = 16;
									break;
								case Z:
									from.z = to.z;
									to.z = 16;
									break;
							}
						}
					}

					FluidStack stack = base.copy();
					stack.amount = tank.amount;

					to.y = from.y + (to.y - from.y) * ((float) tank.amount / tank.getCapacity());
					renderFluidCube(part.getWorld(), pos, from, to, new FluidEntry(stack, part.fluid.fluidColor, tank.location), buffer);
				}
			}
		}

		synchronized (part.getPipeItems()) {
			for (PipeItem item : part.getPipeItems()) {
				ItemStack stack = item.getStack();
				if (stack == null || stack.getItem() == null) {
					continue;
				}

				EnumFacing id = item.getDirection();
				float ix, iy, iz;

				if (id == null) {
					ix = 0.5f;
					iy = 0.5f;
					iz = 0.5f;
				} else if (item.isStuck() || (!item.hasReachedCenter() && item.getProgress() == 0.5F)) {
					ix = item.getX();
					iy = item.getY();
					iz = item.getZ();
				} else {
					ix = item.getX() + ((float) id.getFrontOffsetX() * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
					iy = item.getY() + ((float) id.getFrontOffsetY() * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
					iz = item.getZ() + ((float) id.getFrontOffsetZ() * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
				}

				if (id != null) {
					PREDICTIVE_ITEM_RANDOM.setSeed(item.id);

					switch (id.ordinal() >> 1) {
						case 0:
						case 1:
							ix += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
							break;
						case 2:
							iz += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
							break;
					}
				}

				IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, part.getWorld(), null);
				IBlockState state = Blocks.AIR.getDefaultState();
				if (model.isBuiltInRenderer()) {
					model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(Blocks.BARRIER), part.getWorld(), null);
				}

				ITEM_MODEL_TRANSFORMER.isBlock = stack.getItem() instanceof ItemBlock;
				ITEM_MODEL_TRANSFORMER.offset[0] = ix;
				ITEM_MODEL_TRANSFORMER.offset[1] = iy - 0.25f;
				ITEM_MODEL_TRANSFORMER.offset[2] = iz - 0.25f;
				renderer.renderModel(getWorld(), ModelTransformer.transform(model, state, 0L, ITEM_MODEL_TRANSFORMER), state, pos, buffer, false);
			}
		}
	}

	@Override
	public void renderMultipartAt(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage) {
	}

	public void clearCache() {
		fluidModelCache.invalidateAll();
	}
}
