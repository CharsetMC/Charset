/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.audio.storage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.Properties;

public class TileRecordPlayerRenderer extends TileEntitySpecialRenderer<TileRecordPlayer> {
	public static final TileRecordPlayerRenderer INSTANCE = new TileRecordPlayerRenderer();
	protected IModel arm;

	private TileRecordPlayerRenderer() {

	}

	@Override
	public void render(TileRecordPlayer tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (tile == null) {
			return;
		}

		IBlockState state = getWorld().getBlockState(tile.getPos());
		ItemStack stack = tile.getStack();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		GlStateManager.translate(0.5f, 0.5f, 0.5f);
		GlStateManager.rotate(180.0f - state.getValue(Properties.FACING4).getHorizontalAngle(), 0, 1, 0);
		GlStateManager.translate(-0.5f, -0.5f, -0.5f);

		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		IBakedModel bakedModel = arm.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.125f, 0, 0.125f);
		GlStateManager.rotate(tile.getArmRotationClient(), 0, 1, 0);
		GlStateManager.translate(-0.125f, 0, -0.125f);
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(
				bakedModel, 1.0f, 1.0f, 1.0f, 1.0f
		);
		GlStateManager.popMatrix();

		if (!stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(0.5, 0.5, -0.625 - 0.0625/4);
			handleRenderItem(stack, tile, partialTicks);
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();

		GlStateManager.disableBlend();
		RenderHelper.enableStandardItemLighting();
	}

	public void handleRenderItem(ItemStack is, TileRecordPlayer player, float partialTicks) {
		float rotation = player.getDiscRotation() + (player.isDiscSpinning() ? (player.getDiscRotationSpeed() * partialTicks) : 0);
		GlStateManager.rotate((float) -(rotation % 360.0), 0, 0, 1);
		GlStateManager.scale(0.625F, 0.625F, 0.625F);
		GlStateManager.pushAttrib();
		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().renderItem(is, ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popAttrib();
	}
}
