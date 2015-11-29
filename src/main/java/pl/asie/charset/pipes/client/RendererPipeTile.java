package pl.asie.charset.pipes.client;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import pl.asie.charset.pipes.PipeItem;
import pl.asie.charset.pipes.TilePipe;

public class RendererPipeTile extends TileEntitySpecialRenderer {
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();

	private static final RenderItem RENDER_ITEM = new RenderItem() {
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
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
		if (tile == null) {
			return;
		}

		TilePipe tilePipe = (TilePipe) tile;

		if (false) {
			TileEntity testTile = DimensionManager.getWorld(tile.getWorldObj().provider.dimensionId).getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord);
			if (testTile instanceof TilePipe) {
				tilePipe = (TilePipe) testTile;
			}
		}

		// TODO: HACK
		synchronized (tilePipe) {
			/* if (tilePipe.getPipeItems().size() > 1) {
				System.out.println("Rendering " + tilePipe.getPipeItems().size() + " items");
			} */

			for (PipeItem item : tilePipe.getPipeItems()) {
				EntityItem itemEntity = new EntityItem(tilePipe.getWorldObj(), tilePipe.xCoord, tilePipe.yCoord, tilePipe.zCoord, item.getStack());
				itemEntity.age = 0;
				itemEntity.hoverStart = 0;

				ForgeDirection id = item.getDirection();
				double ix, iy, iz;

				if (item.isStuck() || (!item.hasReachedCenter() && item.getX() == 0.5F && item.getY() == 0.5F && item.getZ() == 0.5F)) {
					ix = item.getX();
					iy = item.getY();
					iz = item.getZ();
				} else {
					ix = item.getX() + ((float) id.offsetX * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
					iy = item.getY() + ((float) id.offsetY * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
					iz = item.getZ() + ((float) id.offsetZ * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
				}

				PREDICTIVE_ITEM_RANDOM.setSeed(item.id);
				switch (id.ordinal() >> 1) {
					case 0:
					case 1:
						ix += PREDICTIVE_ITEM_RANDOM.nextDouble() * 0.01;
						break;
					case 2:
						iz += PREDICTIVE_ITEM_RANDOM.nextDouble() * 0.01;
						break;
				}

				GL11.glPushMatrix();
				if (item.getStack().getItem() instanceof ItemBlock) {
					GL11.glTranslated(x + ix, y + iy, z + iz);
					if (item.getStack().stackSize > 20) {
						GL11.glScalef(0.8F, 0.8F, 0.8F);
					} else if (item.getStack().stackSize > 1) {

					} else {
						GL11.glScalef(1.33F, 1.33F, 1.33F);
					}
				} else {
					GL11.glTranslated(x + ix, y + iy - 0.125, z + iz);
				}

				RENDER_ITEM.setRenderManager(RenderManager.instance);
				RENDER_ITEM.doRender(itemEntity, 0, 0, 0, 0.0f, 0.0f);

				GL11.glPopMatrix();
			}
		}
	}
}
