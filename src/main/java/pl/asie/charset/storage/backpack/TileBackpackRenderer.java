package pl.asie.charset.storage.backpack;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;

import pl.asie.charset.lib.refs.Properties;
import pl.asie.charset.storage.ProxyClient;

/**
 * Created by asie on 1/10/16.
 */
public class TileBackpackRenderer extends TileEntitySpecialRenderer<TileBackpack> {
	@Override
	public void renderTileEntityAt(TileBackpack te, double x, double y, double z, float partialTicks, int destroyStage) {
		BlockPos pos = te.getPos();
		IBlockState state = getWorld().getBlockState(pos);

		if (state.getBlock() instanceof BlockBackpack) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();

			BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

			GlStateManager.translate(x - pos.getX(), y - pos.getY(), z - pos.getZ());

			Tessellator.getInstance().getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			renderer.renderModel(getWorld(), ProxyClient.backpackTopModel[state.getValue(Properties.FACING4).ordinal() - 2], state, pos, Tessellator.getInstance().getWorldRenderer(), false);
			Tessellator.getInstance().draw();

			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
