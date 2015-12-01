package pl.asie.charset.pipes.client;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.DimensionManager;

import pl.asie.charset.pipes.PipeItem;
import pl.asie.charset.pipes.TilePipe;

public class RendererPipeTile extends TileEntitySpecialRenderer {
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();
	private static final float ITEM_RANDOM_OFFSET = 0.01F;

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

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
		if (tile == null) {
			return;
		}

		TilePipe tilePipe = (TilePipe) tile;

		if (false) {
			TileEntity testTile = DimensionManager.getWorld(tile.getWorld().provider.getDimensionId()).getTileEntity(tile.getPos());
			if (testTile instanceof TilePipe) {
				tilePipe = (TilePipe) testTile;
			}
		}

		synchronized (tilePipe.getPipeItems()) {
			for (PipeItem item : tilePipe.getPipeItems()) {
				EntityItem itemEntity = new EntityItem(tilePipe.getWorld(), tilePipe.getPos().getX(), tilePipe.getPos().getY(), tilePipe.getPos().getZ(), item.getStack());
				itemEntity.hoverStart = 0;

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

				GlStateManager.pushMatrix();
				GlStateManager.translate(x + ix, y + iy - 0.25, z + iz);

				RENDER_ITEM.doRender(itemEntity, 0, 0, 0, 0.0f, 0.0f);

				GlStateManager.popMatrix();
			}
		}
	}
}
