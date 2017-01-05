package pl.asie.charset.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class TweakCarryRender {
	private static final Minecraft mc = Minecraft.getMinecraft();

	/* @SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
			Entity rve = Minecraft.getMinecraft().getRenderViewEntity();
			if (!(rve instanceof EntityPlayer)) {
				return;
			}
			EntityPlayer player = (EntityPlayer) rve;
			CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
			if (carryHandler != null && carryHandler.isCarrying()) {
				event.setCanceled(true);
			}
		}
	} */

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onRenderHand(RenderHandEvent event) {
		Entity rve = Minecraft.getMinecraft().getRenderViewEntity();
		if (!(rve instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer player = (EntityPlayer) rve;
		float partialTicks = event.getPartialTicks();

		CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
		if (carryHandler != null && carryHandler.isCarrying()) {
			event.setCanceled(true);
			if (this.mc.gameSettings.thirdPersonView != 0 || this.mc.gameSettings.hideGUI) {
				return;
			}

			Minecraft.getMinecraft().entityRenderer.enableLightmap();

			GlStateManager.pushMatrix();

			/* GlStateManager.pushMatrix();
			GlStateManager.rotate(rotX, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(rotY, 0.0F, 1.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.popMatrix(); */

			float yaw = carryHandler.getGrabbedYaw() + 45.0f;
			while (yaw < 0)
				yaw += 360.0f;
			yaw = yaw - (yaw % 90);

			GlStateManager.translate(0, player.isSneaking() ? -0.65 : -0.75, -1);
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableBlend();
			GlStateManager.rotate(yaw - 180, 0, 1, 0);
			GlStateManager.translate(-0.5, -0.5, -0.5);

			TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

			try {
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();

				buffer.setTranslation(0, -64, 0);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					tessellator.draw();
				}

				buffer.setTranslation(0, 0, 0);

				TileEntity tile = carryHandler.getBlockAccess().getTileEntity(CarryHandler.ACCESS_POS);
				if (tile != null) {
					//RenderHelper.enableStandardItemLighting();
					/* int i = carryHandler.getBlockAccess().getCombinedLight(CarryHandler.ACCESS_POS, 0);
					int j = i % 65536;
					int k = i / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k); */
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

					try {
						TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0, 0, 0, partialTicks);
					} catch (Exception e) {

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

			GlStateManager.disableBlend();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().entityRenderer.disableLightmap();
		}
	}
}
