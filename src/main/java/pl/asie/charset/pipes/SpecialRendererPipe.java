package pl.asie.charset.pipes;

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
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.ModelTransformer;
import pl.asie.charset.lib.render.SimpleBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

public class SpecialRendererPipe extends MultipartSpecialRenderer<PartPipe> {
	protected static BlockModelRenderer renderer;
	protected static RenderItem renderItem;
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();
	private static final float ITEM_RANDOM_OFFSET = 0.01F;

	private static final float TANK_MIN = 4.01f;
	private static final float TANK_MAX = 11.99f;
	private static final Vector3f[] TANK_FROM = new Vector3f[] {
			new Vector3f(TANK_MIN, 0, TANK_MIN),
			new Vector3f(TANK_MIN, TANK_MAX, TANK_MIN),
			new Vector3f(0, TANK_MIN, TANK_MIN),
			new Vector3f(TANK_MAX, TANK_MIN, TANK_MIN),
			new Vector3f(TANK_MIN, TANK_MIN, 0),
			new Vector3f(TANK_MIN, TANK_MIN, TANK_MAX),
			new Vector3f(TANK_MIN, TANK_MIN, TANK_MIN)
	};
	private final Vector3f[] TANK_TO = new Vector3f[] {
			new Vector3f(TANK_MAX, TANK_MIN, TANK_MAX),
			new Vector3f(TANK_MAX, 16, TANK_MAX),
			new Vector3f(TANK_MIN, TANK_MAX, TANK_MAX),
			new Vector3f(16, TANK_MAX, TANK_MAX),
			new Vector3f(TANK_MAX, TANK_MAX, TANK_MIN),
			new Vector3f(TANK_MAX, TANK_MAX, 16),
			new Vector3f(TANK_MAX, TANK_MAX, TANK_MAX)
	};

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
		private final float[] offset = new float[3];
		private EnumFacing direction;
		private boolean isBlock;

		@Override
		public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
			if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				float size = isBlock ? 0.35f : 0.4f;
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
					data[i] = ((data[i] - 0.5f) * size) + offset[i];
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
		smodel.addQuad(null, RenderUtils.BAKERY.makeBakedQuad(
				new Vector3f(from.x, from.y, from.z),
				new Vector3f(to.x, from.y, to.z),
				-1, sprite, EnumFacing.DOWN, ModelRotation.X0_Y0, false
		));
		smodel.addQuad(null, RenderUtils.BAKERY.makeBakedQuad(
				new Vector3f(from.x, to.y, from.z),
				new Vector3f(to.x, to.y, to.z),
				-1, sprite, EnumFacing.UP, ModelRotation.X0_Y0, false
		));
		smodel.addQuad(null, RenderUtils.BAKERY.makeBakedQuad(
				new Vector3f(from.x, from.y, from.z),
				new Vector3f(from.x, to.y, to.z),
				-1, sprite, EnumFacing.WEST, ModelRotation.X0_Y0, false
		));
		smodel.addQuad(null, RenderUtils.BAKERY.makeBakedQuad(
				new Vector3f(to.x, from.y, from.z),
				new Vector3f(to.x, to.y, to.z),
				-1, sprite, EnumFacing.EAST, ModelRotation.X0_Y0, false
		));
		smodel.addQuad(null, RenderUtils.BAKERY.makeBakedQuad(
				new Vector3f(from.x, from.y, from.z),
				new Vector3f(to.x, to.y, from.z),
				-1, sprite, EnumFacing.NORTH, ModelRotation.X0_Y0, false
		));
		smodel.addQuad(null, RenderUtils.BAKERY.makeBakedQuad(
				new Vector3f(from.x, from.y, to.z),
				new Vector3f(to.x, to.y, to.z),
				-1, sprite, EnumFacing.SOUTH, ModelRotation.X0_Y0, false
		));

		if (entry.color == -1) {
			return smodel;
		} else {
			return ModelTransformer.transform(smodel, entry.stack.getFluid().getBlock().getDefaultState(), 0L, new FluidColorTransformer(entry.color));
		}
	}

	private final Set<PipeItem> SLOW_ITEMS = new HashSet<>();

	@Override
	public void renderMultipartFast(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
		if (part == null) {
			return;
		}

		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		if (renderItem == null) {
			renderItem = Minecraft.getMinecraft().getRenderItem();
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

		float ix, iy, iz;

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

				if (id == null) {
					ix = 0.5f;
					iy = 0.5f;
					iz = 0.5f;
				} else if (item.isStuck() || (!item.hasReachedCenter() && item.getProgress() == 0.5F)) {
					ix = item.getX();
					iy = item.getY();
					iz = item.getZ();
				} else {
					float partialMul = partialTicks * PipeItem.SPEED / PipeItem.MAX_PROGRESS;
					ix = item.getX() + (partialMul * id.getFrontOffsetX());
					iy = item.getY() + (partialMul * id.getFrontOffsetY());
					iz = item.getZ() + (partialMul * id.getFrontOffsetZ());
				}

				if (id != null) {
					PREDICTIVE_ITEM_RANDOM.setSeed(item.id);

					switch (id.getAxis()) {
						case Y:
						case X:
							ix += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
							break;
						case Z:
							iz += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
							break;
					}
				}

				ITEM_MODEL_TRANSFORMER.isBlock = stack.getItem() instanceof ItemBlock;
				ITEM_MODEL_TRANSFORMER.direction = id;
				ITEM_MODEL_TRANSFORMER.offset[0] = ix;
				ITEM_MODEL_TRANSFORMER.offset[1] = iy;
				ITEM_MODEL_TRANSFORMER.offset[2] = iz;

				renderer.renderModel(getWorld(), ModelTransformer.transform(model, DEFAULT_STATE, 0L, ITEM_MODEL_TRANSFORMER), DEFAULT_STATE, pos, buffer, false);
			}
		}
	}

	@Override
	public void renderMultipartAt(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage) {
		if (part == null) {
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		renderMultipartFast(part, x, y, z, partialTicks, destroyStage, buffer);
		Tessellator.getInstance().draw();

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		if (SLOW_ITEMS.size() > 0) {
			float ix, iy, iz;

			for (PipeItem item : SLOW_ITEMS) {
				EnumFacing id = item.getDirection();
				ItemStack stack = item.getStack();

				if (id == null) {
					ix = 0.5f;
					iy = 0.5f;
					iz = 0.5f;
				} else if (item.isStuck() || (!item.hasReachedCenter() && item.getProgress() == 0.5F)) {
					ix = item.getX();
					iy = item.getY();
					iz = item.getZ();
				} else {
					float partialMul = partialTicks * PipeItem.SPEED / PipeItem.MAX_PROGRESS;
					ix = item.getX() + (partialMul * id.getFrontOffsetX());
					iy = item.getY() + (partialMul * id.getFrontOffsetY());
					iz = item.getZ() + (partialMul * id.getFrontOffsetZ());
				}

				GlStateManager.pushMatrix();

				if (id != null) {
					PREDICTIVE_ITEM_RANDOM.setSeed(item.id);

					switch (id.getAxis()) {
						case Y:
						case X:
							ix += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
							break;
						case Z:
							iz += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
							break;
					}
				}

				IBakedModel model = itemModelCache.getIfPresent(stack);
				if (model == null) {
					model = renderItem.getItemModelWithOverrides(stack, part.getWorld(), null);
				}

				GlStateManager.translate(x + ix, y + iy, z + iz);

				if (stack.getItem() instanceof ItemBlock) {
					GlStateManager.scale(0.35f, 0.35f, 0.35f);
				} else {
					GlStateManager.scale(0.4f, 0.4f, 0.4f);
				}

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
