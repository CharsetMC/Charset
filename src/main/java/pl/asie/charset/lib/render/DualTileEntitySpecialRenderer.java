package pl.asie.charset.lib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/**
 * Created by asie on 11/26/16.
 */
public class DualTileEntitySpecialRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {
	protected final Minecraft mc = Minecraft.getMinecraft();

	protected void renderTileEntityFastFromSlow(T part, double x, double y, double z, float partialTicks, int destroyStage) {
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();

		VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		renderTileEntityFast(part, x, y, z, partialTicks, destroyStage, buffer);
		Tessellator.getInstance().draw();

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}
