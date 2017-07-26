package pl.asie.charset.module.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.MathUtils;

import java.util.HashSet;

public class TweakCarryRender {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final HashSet<Class> caughtExceptionTileRenderers = new HashSet<>();

	/* @SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
			Entity rve = Minecraft.getMinecraft().getRenderViewEntity();
			if (!(rve instanceof EntityPlayer)) {
				return;
			}
			EntityPlayer player = (EntityPlayer) rve;
			CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
			if (carryHandler != null && carryHandler.isCarrying()) {
				event.setCanceled(true);
			}
		}
	} */

	private void renderCarriedBlock(CarryHandler carryHandler, float partialTicks) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

		try {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			if (carryHandler.getBlockState().getRenderType() == EnumBlockRenderType.MODEL) {
				buffer.setTranslation(0, -64, 0);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

				try {
					IBlockState renderState = carryHandler.getBlockState().getActualState(carryHandler.getBlockAccess(), CarryHandler.ACCESS_POS);
					IBlockState renderStateExt = carryHandler.getBlockState().getBlock().getExtendedState(renderState, carryHandler.getBlockAccess(), CarryHandler.ACCESS_POS);

					BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
					IBakedModel model = brd.getModelForState(renderState);
					brd.getBlockModelRenderer().renderModelFlat(carryHandler.getBlockAccess(),
							model, renderStateExt,
							CarryHandler.ACCESS_POS, buffer, false, 0L
					);
				} catch (Exception e) {
					e.printStackTrace();
				}

				tessellator.draw();
				buffer.setTranslation(0, 0, 0);
			}

			TileEntity tile = carryHandler.getBlockAccess().getTileEntity(CarryHandler.ACCESS_POS);
			if (tile != null) {
				RenderHelper.enableStandardItemLighting();
				int i = carryHandler.getBlockAccess().getCombinedLight(CarryHandler.ACCESS_POS, 0);
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

				if (tile instanceof TileEntityMobSpawner) {
					// TODO: Refactor into ICustomCarryHandler
					if (carryHandler.spawnerLogic != null) {
						GlStateManager.pushMatrix();
						GlStateManager.translate(0.5F, 0, 0.5F);
						TileEntityMobSpawnerRenderer.renderMob(carryHandler.spawnerLogic, 0, 0, 0, partialTicks);
						GlStateManager.popMatrix();
					}
				} else {
					try {
						TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0, partialTicks);
					} catch (Exception e) {
						if (!caughtExceptionTileRenderers.contains(tile.getClass())) {
							e.printStackTrace();
							ModCharset.logger.warn("Future exceptions from this tile entity will be hidden.");
							caughtExceptionTileRenderers.add(tile.getClass());
						}
						// Hack of the Year award for the Least Graceful Recovery
						buffer.setTranslation(0, 0, 0);
						boolean caught = false;
						while (!caught) {
							try {
								tessellator.draw();
							} catch (IllegalStateException ee) {
								caught = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderPlayerEvent(RenderPlayerEvent.Post event) {
		EntityPlayer player = event.getEntityPlayer();
		CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
		if (carryHandler != null && carryHandler.isCarrying()) {
			float partialTicks = event.getPartialRenderTick();
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

			GlStateManager.color(1, 1, 1,1);
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.enableRescaleNormal();
			GlStateManager.rotate(-MathUtils.interpolate(player.prevRenderYawOffset, player.renderYawOffset, partialTicks), 0, 1, 0);
			if (player.isSneaking())
				GlStateManager.rotate(45.0f, 1, 0, 0);
			GlStateManager.translate(0, player.isSneaking() ? 0.35 : 0.8, player.isSneaking() ? -0.3 : 0.3);
			GlStateManager.scale(0.5, 0.5, 0.5);
			GlStateManager.rotate(yaw, 0, 1, 0);
			GlStateManager.translate(-0.5, -0.5, -0.5);

			renderCarriedBlock(carryHandler, partialTicks);

			GlStateManager.disableBlend();
			GlStateManager.disableRescaleNormal();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().entityRenderer.disableLightmap();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@SideOnly(Side.CLIENT)
	public void onRenderHand(RenderHandEvent event) {
		Entity rve = Minecraft.getMinecraft().getRenderViewEntity();
		if (!(rve instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer player = (EntityPlayer) rve;
		float partialTicks = event.getPartialTicks();

		CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
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

			GlStateManager.color(1, 1, 1,1);
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.translate(0, player.isSneaking() ? -0.65 : -0.75, -1);
			GlStateManager.enableRescaleNormal();
			GlStateManager.rotate(yaw - 180, 0, 1, 0);
			GlStateManager.translate(-0.5, -0.5, -0.5);

			renderCarriedBlock(carryHandler, partialTicks);

			GlStateManager.disableBlend();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().entityRenderer.disableLightmap();
		}
	}
}
