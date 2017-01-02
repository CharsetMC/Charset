package pl.asie.charset.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Created by asie on 1/2/17.
 */
public class TweakCarryRender {
	@SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onRenderHand(RenderHandEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		float partialTicks = event.getPartialTicks();

		CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
		if (carryHandler != null && carryHandler.isCarrying()) {
			event.setCanceled(true);
			Minecraft.getMinecraft().entityRenderer.enableLightmap();

			GlStateManager.pushMatrix();
			float rotX = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
			float rotY = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;

			GlStateManager.pushMatrix();
			GlStateManager.rotate(rotX, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(rotY, 0.0F, 1.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.popMatrix();

			GlStateManager.translate(-0.5, -1.25, -1.5);
			GlStateManager.enableRescaleNormal();

			try {
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();

				buffer.setTranslation(0, -64, 0);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

				IBlockState renderState = carryHandler.getBlockState().getActualState(carryHandler.getBlockAccess(), CarryHandler.ACCESS_POS);
				IBlockState renderStateExt = carryHandler.getBlockState().getBlock().getExtendedState(renderState, carryHandler.getBlockAccess(), CarryHandler.ACCESS_POS);

				BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
				IBakedModel model = brd.getModelForState(renderState);
				if (carryHandler.getBlockState().getRenderType() == EnumBlockRenderType.MODEL) {
					brd.getBlockModelRenderer().renderModelFlat(carryHandler.getBlockAccess(),
							model, renderStateExt,
							CarryHandler.ACCESS_POS, buffer, false, 0L
					);
				}

				tessellator.draw();
				buffer.setTranslation(0, 0, 0);

				TileEntity tile = carryHandler.getBlockAccess().getTileEntity(CarryHandler.ACCESS_POS);
				if (tile != null) {
					//RenderHelper.enableStandardItemLighting();
					int i = carryHandler.getBlockAccess().getCombinedLight(CarryHandler.ACCESS_POS, 0);
					int j = i % 65536;
					int k = i / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

					try {
						TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0, 0, 0, partialTicks);
					} catch (Exception e) {

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().entityRenderer.disableLightmap();
		}
	}
}
