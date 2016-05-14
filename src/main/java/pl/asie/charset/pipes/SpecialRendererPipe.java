package pl.asie.charset.pipes;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.CharsetBakedModel;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.lib.render.SimpleBakedModel;
import pl.asie.charset.lib.utils.ClientUtils;

public class SpecialRendererPipe extends MultipartSpecialRenderer<PartPipe> {
	protected static BlockModelRenderer renderer;
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();
	private static final float ITEM_RANDOM_OFFSET = 0.01F;
	private final CharsetFaceBakery faceBakery = new CharsetFaceBakery();

	private static final RenderEntityItem RENDER_ITEM = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
		@Override
		public boolean shouldBob() {
			return false;
		}

		@Override
		public boolean shouldSpreadItems() {
			return false;
		}
	};

	private class FluidEntry {
		public final FluidStack stack;
		public final int side;
		private final int hash;

		public FluidEntry(FluidStack stack, EnumFacing side) {
			this.stack = stack;
			this.side = side != null ? side.ordinal() : 6;
			this.hash = Objects.hash(stack, side);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof FluidEntry)) {
				return false;
			}

			FluidEntry f = (FluidEntry) o;
			return f.stack.getFluid() == stack.getFluid() && f.stack.amount == stack.amount && f.side == side;
		}

		@Override
		public int hashCode() {
			return this.hash;
		}
	}

	private final Cache<FluidEntry, SimpleBakedModel> fluidModelCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	private void renderFluidCube(IBlockAccess world, BlockPos pos, Vector3f from, Vector3f to, FluidEntry entry, int color, boolean isFast) {
		SimpleBakedModel model = fluidModelCache.getIfPresent(entry);
		if (model == null) {
			TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
			TextureAtlasSprite sprite = map.getTextureExtry(entry.stack.getFluid().getStill().toString());
			if (sprite == null) {
				sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
				if (sprite == null) {
					return;
				}
			}

			model = new SimpleBakedModel();
			model.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, from.z),
					new Vector3f(to.x, from.y, to.z),
					-1, sprite, EnumFacing.DOWN, ModelRotation.X0_Y0, false
			));
			model.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, to.y, from.z),
					new Vector3f(to.x, to.y, to.z),
					-1, sprite, EnumFacing.UP, ModelRotation.X0_Y0, false
			));
			model.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, from.z),
					new Vector3f(from.x, to.y, to.z),
					-1, sprite, EnumFacing.WEST, ModelRotation.X0_Y0, false
			));
			model.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(to.x, from.y, from.z),
					new Vector3f(to.x, to.y, to.z),
					-1, sprite, EnumFacing.EAST, ModelRotation.X0_Y0, false
			));
			model.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, from.z),
					new Vector3f(to.x, to.y, from.z),
					-1, sprite, EnumFacing.NORTH, ModelRotation.X0_Y0, false
			));
			model.addQuad(null, faceBakery.makeBakedQuad(
					new Vector3f(from.x, from.y, to.z),
					new Vector3f(to.x, to.y, to.z),
					-1, sprite, EnumFacing.SOUTH, ModelRotation.X0_Y0, false
			));

			fluidModelCache.put(entry, model);
		}

		if (isFast) {
			// TODO: count in color
			renderer.renderModel(world, model, entry.stack.getFluid().getBlock().getDefaultState(), pos, Tessellator.getInstance().getBuffer(), false);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();

			ClientUtils.glColor(color);

			BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

			Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			renderer.renderModel(world, model, entry.stack.getFluid().getBlock().getDefaultState(), pos, Tessellator.getInstance().getBuffer(), false);
			Tessellator.getInstance().draw();

			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}

	private void renderMultipart(PartPipe part, double x, double y, double z, float partialTicks, VertexBuffer buffer) {
		boolean isFast = buffer != null;
		if (part == null) {
			return;
		}

		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		BlockPos pos = part.getPos();

		if (isFast) {
			buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x - part.getPos().getX(), y - part.getPos().getY(), z - part.getPos().getZ());
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}

		for (PipeFluidContainer.Tank tank : part.fluid.tanks) {
			if (tank.stack != null) {
				Vector3f from = new Vector3f(4.01f, 4.01f, 4.01f);
				Vector3f to = new Vector3f(11.99f, 11.99f, 11.99f);

				if (tank.location != null) {
					if (tank.location.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
						switch (tank.location.getAxis()) {
							case X:
								from.x = 0;
								break;
							case Y:
								from.y = 0;
								break;
							case Z:
								from.z = 0;
								break;
						}
					} else {
						switch (tank.location.getAxis()) {
							case X:
								to.x = 16;
								break;
							case Y:
								to.y = 16;
								break;
							case Z:
								to.z = 16;
								break;
						}
					}
				}

				to.y = from.y + (to.y - from.y) * ((float) tank.stack.amount / tank.getCapacity());
				renderFluidCube(part.getWorld(), pos, from, to, new FluidEntry(tank.stack, tank.location), tank.color, isFast);
			}
		}

		if (!isFast) {
			GlStateManager.popMatrix();
		}

		synchronized (part.getPipeItems()) {
			for (PipeItem item : part.getPipeItems()) {
				EnumFacing id = item.getDirection();
				double ix, iy, iz;

				if (id == null) {
					ix = 0.5;
					iy = 0.5;
					iz = 0.5;
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

				if (isFast) {
					IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(item.stack, part.getWorld(), null);
					if (model.isBuiltInRenderer()) {
						model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(Blocks.BARRIER), part.getWorld(), null);
					}

					buffer.setTranslation(x - pos.getX() + ix, y - pos.getY() + iy - 0.25, z - pos.getZ() + iz);
					renderer.renderModel(getWorld(), model, Blocks.AIR.getDefaultState(), pos, buffer, false);
				} else {
					EntityItem itemEntity = new EntityItem(part.getWorld(), part.getPos().getX(), part.getPos().getY(), part.getPos().getZ(), item.getStack());
					itemEntity.hoverStart = 0;

					GlStateManager.pushMatrix();
					GlStateManager.translate(x + ix, y + iy - 0.25, z + iz);

					RENDER_ITEM.doRender(itemEntity, 0, 0, 0, 0.0f, 0.0f);

					GlStateManager.popMatrix();
				}
			}
		}
	}

	@Override
	public void renderMultipartFast(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
		renderMultipart(part, x, y, z, partialTicks, buffer);
	}

	@Override
	public void renderMultipartAt(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage) {
		renderMultipart(part, x, y, z, partialTicks, null);
	}

	public void clearCache() {
		fluidModelCache.invalidateAll();
	}
}
