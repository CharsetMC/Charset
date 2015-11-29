package pl.asie.charset.pipes.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import pl.asie.charset.pipes.TileShifter;

public class RendererShifterTile extends TileEntitySpecialRenderer {
	private static final RenderItem RENDER_ITEM = new RenderItem() {
		@Override
		public boolean shouldBob() {
			return false;
		}

		@Override
		public byte getMiniBlockCount(ItemStack stack, byte original) {
			return original > 1 ? 1 : original;
		}

		@Override
		public byte getMiniItemCount(ItemStack stack, byte original) {
			return original > 1 ? 1 : original;
		}
	};

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
		TileShifter shifter = (TileShifter) tileEntity;

		ItemStack[] filters = shifter.getFilters();

		for (int i = 0; i < filters.length; i++) {
			if (filters[i] == null) {
				continue;
			}

			EntityItem entityitem = new EntityItem(shifter.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, filters[i]);
			ForgeDirection itemDir = ForgeDirection.getOrientation(i);

			float translationConstant = 0.49375F;
			
			entityitem.hoverStart = 0.0F;
			GL11.glPushMatrix();
			GL11.glTranslatef(translationConstant * itemDir.offsetX, translationConstant * itemDir.offsetY, translationConstant * itemDir.offsetZ);
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

			switch (itemDir) {
				case NORTH:
					GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
					break;
				case SOUTH:
					break;
				case WEST:
					GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
					break;
				case EAST:
					GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
					break;
				case UP:
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					break;
				case DOWN:
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					break;
			}

			if (filters[i].getItem() instanceof ItemBlock) {
				GL11.glTranslatef(0, -0.0625F * 1.5F, -0.0625F);
				GL11.glScalef(1.5F, 1.5F, 1.5F);
			} else {
				GL11.glTranslatef(0, -0.125F, 0);
				GL11.glScalef(1.33F, 1.33F, 1.33F);
			}

			int l = tileEntity.getWorldObj().getLightBrightnessForSkyBlocks(
					tileEntity.xCoord + itemDir.offsetX,
					tileEntity.yCoord + itemDir.offsetY,
					tileEntity.zCoord + itemDir.offsetZ,
					i ^ 1);
			int j = l % 65536;
			int k = l / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);

			RenderItem.renderInFrame = true;
			RENDER_ITEM.setRenderManager(RenderManager.instance);
			RENDER_ITEM.doRender(entityitem, 0, 0, 0, 0.0f, 0.0f);
			RenderItem.renderInFrame = false;

			GL11.glPopMatrix();
		}
	}
}
