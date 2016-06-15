/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.storage.backpack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.animation.FastTESR;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.storage.ProxyClient;

public final class RendererBackpack {
	public static class Tile extends FastTESR<TileBackpack> {
		protected static BlockModelRenderer renderer;

		@Override
		public void renderTileEntityFast(TileBackpack te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer worldRenderer) {
			if (renderer == null) {
				renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
			}

			BlockPos pos = te.getPos();
			IBlockState state = getWorld().getBlockState(pos);

			if (state.getBlock() instanceof BlockBackpack) {
				worldRenderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
				renderer.renderModel(getWorld(), ProxyClient.backpackTopModel[state.getValue(Properties.FACING4).ordinal() - 2], state, pos, worldRenderer, false);
			}
		}
	}

	public static class Armor {
		protected float interpolateRotation(float par1, float par2, float par3) {
			float f;

			for (f = par2 - par1; f < -180.0F; f += 360.0F) {
			}

			while (f >= 180.0F) {
				f -= 360.0F;
			}

			return par1 + par3 * f;
		}

		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void onRenderArmor(RenderPlayerEvent.Pre event) {
			ItemStack backpack = event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			if (backpack != null && backpack.getItem() instanceof ItemBackpack) {
				BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

				GlStateManager.pushMatrix();
				GlStateManager.scale(0.75, 0.75, 0.75);
				GlStateManager.translate(
						event.getEntity().posX - Minecraft.getMinecraft().thePlayer.posX,
						event.getEntity().posY - Minecraft.getMinecraft().thePlayer.posY,
						event.getEntity().posZ - Minecraft.getMinecraft().thePlayer.posZ
				);
				GlStateManager.rotate(-interpolateRotation(event.getEntityPlayer().prevRenderYawOffset, event.getEntityPlayer().renderYawOffset, event.getPartialRenderTick()), 0.0F, 1.0F, 0.0F);
				if (event.getEntityPlayer().isSneaking()) {
					GlStateManager.translate(-0.5, 1.125, -0.845);
					GlStateManager.rotate(30.0f, 1.0f, 0.0f, 0.0f);
					GlStateManager.translate(0, 0.175, -0.35);
				} else {
					GlStateManager.translate(-0.5, 1.2, -0.845);
				}

				int i = ((ItemBackpack) backpack.getItem()).getColor(backpack);
				if (i == -1) {
					i = BlockBackpack.DEFAULT_COLOR;
				}

				if (EntityRenderer.anaglyphEnable) {
					i = TextureUtil.anaglyphColor(i);
				}

				float f = (float) (i >> 16 & 255) / 255.0F;
				float f1 = (float) (i >> 8 & 255) / 255.0F;
				float f2 = (float) (i & 255) / 255.0F;

				renderer.renderModelBrightnessColor(ProxyClient.backpackModel[1], 1.0f, f, f1, f2);
				renderer.renderModelBrightnessColor(ProxyClient.backpackTopModel[1], 1.0f, f, f1, f2);

				GlStateManager.popMatrix();
			}
		}
	}

	private RendererBackpack() {

	}
}
