package pl.asie.charset.lib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
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

	protected void renderTileEntityFastFromSlow(T part, double x, double y, double z, float partialTicks, int destroyStage, float todo_figure_me_out) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		if (Minecraft.isAmbientOcclusionEnabled())
		{
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		}
		else
		{
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		renderTileEntityFast(part, x, y, z, partialTicks, destroyStage, todo_figure_me_out, builder);
		builder.setTranslation(0, 0, 0);

		tessellator.draw();

		RenderHelper.enableStandardItemLighting();
	}
}
