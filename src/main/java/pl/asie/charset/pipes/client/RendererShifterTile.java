package pl.asie.charset.pipes.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import pl.asie.charset.pipes.TileShifter;

public class RendererShifterTile extends TileEntitySpecialRenderer {
	private final Minecraft mc = Minecraft.getMinecraft();
	private final RenderManager renderManager = mc.getRenderManager();
	private final RenderItem itemRenderer = mc.getRenderItem();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
		TileShifter shifter = (TileShifter) tileEntity;

		ItemStack[] filters = shifter.getFilters();

		for (int i = 0; i < filters.length; i++) {
			if (filters[i] == null) {
				continue;
			}

			EntityItem entityitem = new EntityItem(shifter.getWorld(), tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), filters[i]);
			EnumFacing itemDir = EnumFacing.getFront(i);

			float translationConstant = 0.49375F;

			entityitem.hoverStart = 0.0F;
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			GL11.glTranslatef(translationConstant * itemDir.getFrontOffsetX(), translationConstant * itemDir.getFrontOffsetY(), translationConstant * itemDir.getFrontOffsetZ());
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
				GL11.glTranslatef(0, 0, -0.0625F);
				GL11.glScalef(2.0F, 2.0F, 2.0F);
			} else {
				GL11.glScalef(1.33F, 1.33F, 1.33F);
			}

			int l = tileEntity.getWorld().getCombinedLight(tileEntity.getPos().offset(itemDir), 15);
			int j = l % 65536;
			int k = l / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);

			TextureAtlasSprite textureatlassprite = null;

			GlStateManager.scale(0.5F, 0.5F, 0.5F);

			if (!this.itemRenderer.shouldRenderItemIn3D(entityitem.getEntityItem()) || filters[i].getItem() instanceof ItemSkull) {
				//GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			}

			GlStateManager.pushAttrib();
			RenderHelper.enableStandardItemLighting();
			this.itemRenderer.func_181564_a(entityitem.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popAttrib();

			if (textureatlassprite != null && textureatlassprite.getFrameCount() > 0)
			{
				textureatlassprite.updateAnimation();
			}

			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
