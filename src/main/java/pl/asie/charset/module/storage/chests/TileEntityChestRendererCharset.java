/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.storage.chests;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelLargeChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.RenderUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TileEntityChestRendererCharset extends TileEntitySpecialRenderer<TileEntityChestCharset> {
	protected static final TileEntityChestRendererCharset INSTANCE = new TileEntityChestRendererCharset();
	protected static final ResourceLocation TEXTURE_NORMAL_DOUBLE = new ResourceLocation("charset_generated:textures/entity/chest/normal_double.png");
	protected static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation("charset_generated:textures/entity/chest/normal.png");
	private static final ResourceLocation[] NORMAL_LOCS = new ResourceLocation[] { TEXTURE_NORMAL, TEXTURE_NORMAL_DOUBLE };
	private static final Map<ItemMaterial, ResourceLocation[]> textureLocMap = new HashMap<>();
	private final ModelChest singleChest = new ModelChest();
	private final ModelLargeChest doubleChest = new ModelLargeChest();

	public static void onResourceManagerReload() {
		textureLocMap.clear();
	}

	public static class Stack extends TileEntityItemStackRenderer {
		@Override
		public void renderByItem(ItemStack stack, float partialTicks) {
			TileEntityChestCharset chest = new TileEntityChestCharset();
			chest.loadFromStack(stack);
			TileEntityChestRendererCharset.INSTANCE.render(
					chest, 0, 0, 0, partialTicks, -1, 1.0F
			);
		}
	}

	protected TileEntityChestRendererCharset() {
	}

	@Override
	public void render(TileEntityChestCharset te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te == null) {
			// GitHub/#352
			ModCharset.logger.error("TileEntityChestCharset null in renderer! This should not happen! This is a bug!");
			new Throwable().printStackTrace();
			return;
		}

		EnumFacing facing = EnumFacing.SOUTH;

		//noinspection ConstantConditions
		if (te.getWorld() != null && te.getPos() != null) {
			IBlockState state = te.getWorld().getBlockState(te.getPos());
			if (state.getBlock() instanceof BlockChestCharset) {
				facing = state.getValue(Properties.FACING4);
			}
		}

		ModelChest model;
		boolean isDouble;

		TileEntityChestCharset neighbor;
		EnumFacing neighborFacing;

		if (te.hasNeighbor()) {
			model = doubleChest;
			neighbor = te.getNeighbor();
			neighborFacing = te.getNeighborFace();
			isDouble = true;

			if (neighborFacing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
				return;
			}
		} else {
			model = singleChest;
			neighbor = null;
			neighborFacing = null;
			isDouble = false;
		}

		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		boolean applyColor = false;

		if (destroyStage >= 0) {
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(isDouble ? 8.0F : 4.0F, 4.0F, 1.0F);
			GlStateManager.translate(0.5/16F, 0.5/16F, 0.5/16F);
			GlStateManager.matrixMode(5888);
		} else {
			ItemMaterial mat = te.material.getMaterial();
			ResourceLocation[] locs = textureLocMap.get(mat);
			if (locs == null) {
				ResourceLocation locDouble = TEXTURE_NORMAL_DOUBLE;
				ResourceLocation locNormal = TEXTURE_NORMAL;
				IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
				boolean found = false;
				try {
					ResourceLocation locOverride = new ResourceLocation("charset", "textures/entity/chest/" + mat.getIdForTexture() + "_double.png");
					if (manager.getResource(locOverride) != null) {
						locDouble = locOverride;
						found = true;
					}
				} catch (IOException e) {
					// pass
				}
				try {
					ResourceLocation locOverride = new ResourceLocation("charset", "textures/entity/chest/" + mat.getIdForTexture() + ".png");
					if (manager.getResource(locOverride) != null) {
						locNormal = locOverride;
						found = true;
					}
				} catch (IOException e) {
					// pass
				}
				if (found) {
					locs = new ResourceLocation[] { locNormal, locDouble };
				} else {
					locs = NORMAL_LOCS;
				}
				textureLocMap.put(mat, locs);
			}
			if (locs == NORMAL_LOCS) {
				applyColor = true;
			}
			this.bindTexture(isDouble ? locs[1] : locs[0]);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		GlStateManager.translate((float)x, (float)y + 1.0F, (float)z + 1.0F);
		GlStateManager.scale(1.0F, -1.0F, -1.0F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);

		int j = 0;
		switch (facing) {
			case NORTH:
				j = 180;
				if (isDouble) {
					GlStateManager.translate(1, 0, 0);
				}
				break;
			case WEST:
				j = 90;
				break;
			case EAST:
				j = 270;
				if (isDouble) {
					GlStateManager.translate(0, 0, -1);
				}
				break;
		}

		GlStateManager.rotate((float)j, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		float f = te.getLidAngle(partialTicks);

		if (neighborFacing != null && neighborFacing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
			f = Math.max(neighbor.getLidAngle(partialTicks), f);
		}

		f = 1.0F - f;
		f = 1.0F - f * f * f;
		model.chestLid.rotateAngleX = -(f * ((float)Math.PI / 2F));
		model.chestKnob.rotateAngleX = model.chestLid.rotateAngleX;

		ItemMaterial mat = te.material.getMaterial();
		if (applyColor) {
			RenderUtils.glColor(
					RenderUtils.getAverageColor(
							RenderUtils.getItemSprite(mat.getStack(), te.getWorld(), null, null),
							RenderUtils.AveragingMode.FULL
					), alpha
			);
		}
		model.chestLid.render(0.0625F);
		model.chestBelow.render(0.0625F);

		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		model.chestKnob.render(0.0625F);

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (destroyStage >= 0) {
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}
}
