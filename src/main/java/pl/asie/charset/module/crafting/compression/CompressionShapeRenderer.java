/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.utils.EntityUtils;
import pl.asie.charset.module.crafting.compression.grid.GridEntry;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import java.util.*;

public class CompressionShapeRenderer {
	public static final CompressionShapeRenderer INSTANCE = new CompressionShapeRenderer();
	private final Set<CompressionShape> shapes = Collections.newSetFromMap(new WeakHashMap<>());

	private CompressionShapeRenderer() {

	}

	public void addShape(CompressionShape shape) {
		shapes.add(shape);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (shapes.isEmpty()) {
			return;
		}

		Iterator<CompressionShape> shapeIterator = shapes.iterator();
		float partialTicks = event.getPartialTicks();
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		double oldSPX = TileEntityRendererDispatcher.staticPlayerX;
		double oldSPY = TileEntityRendererDispatcher.staticPlayerY;
		double oldSPZ = TileEntityRendererDispatcher.staticPlayerZ;
		TileEntityRendererDispatcher.staticPlayerX = 0;
		TileEntityRendererDispatcher.staticPlayerY = 0;
		TileEntityRendererDispatcher.staticPlayerZ = 0;

		GlStateManager.pushMatrix();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();

		Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
		Vec3d cameraPos = EntityUtils.interpolate(cameraEntity, event.getPartialTicks());
		GlStateManager.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		World world = Minecraft.getMinecraft().world;

		while (shapeIterator.hasNext()) {
			CompressionShape shape = shapeIterator.next();
			if (shape.isInvalid() || shape.world.provider.getDimension() != world.provider.getDimension()) {
				shapeIterator.remove();
				continue;
			}

			float progress = shape.getRenderProgress(partialTicks);
			if (progress == -1) {
				shapeIterator.remove();
				continue;
			} else if (progress <= 0) {
				continue;
			}

			double volume = shape.grid.size();
			float[] scale = new float[3];
			int topI = shape.barrelOrientation.top.ordinal() >> 1;
			int leftI = shape.barrelOrientation.getNextRotationOnFace().top.ordinal() >> 1;
			int faceI = shape.barrelOrientation.facing.ordinal() >> 1;
			scale[topI] = 1.0f - ((progress * 2f) / shape.height);
			scale[leftI] = 1.0f - ((progress * 2f) / shape.width);
			scale[faceI] = (float) (volume / (scale[topI] * shape.height * scale[leftI] * shape.width));

			if (scale[faceI] > 1.0f) {
				scale[faceI] = ((scale[faceI] - 1.0f) * 1.61f) + 1.0f;
			}

			Vec3d center = Vec3d.ZERO;
			for (GridEntry barrel : shape.grid) {
				center = center.add(new Vec3d(barrel.getPos()).scale(1.0 / shape.grid.size()));
			}
			center = center.addVector(0.5, 0.5, 0.5);

			GlStateManager.translate(center.x, center.y, center.z);
			GlStateManager.scale(scale[2], scale[0], scale[1]);
			GlStateManager.translate(-center.x, -center.y, -center.z);

			buffer.setTranslation(0, 0, 0);

			Minecraft.getMinecraft().entityRenderer.enableLightmap();
			GlStateManager.disableLighting();

			for (int i = 0; i < 2; i++) {
				for (GridEntry barrel : shape.grid) {
					textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					int cl = world.getCombinedLight(barrel.getPos(), 0);
					int j = cl % 65536;
					int k = cl / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

					if (i == 0) {
						GlStateManager.enableBlend();
						GlStateManager.enableRescaleNormal();
						GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					}

					try {
						if (i == 0) {
							IBlockState state = barrel.getState();
							if (state.getRenderType() == EnumBlockRenderType.MODEL) {
								try {
									IBlockState renderState = state.getActualState(world, barrel.getPos());
									IBlockState renderStateExt = state.getBlock().getExtendedState(renderState, world, barrel.getPos());

									IBakedModel model = brd.getModelForState(renderState);
									brd.getBlockModelRenderer().renderModel(world,
											model, renderStateExt, barrel.getPos(),
											buffer, false, MathHelper.getPositionRandom(barrel.getPos())
									);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						} else {
							TileEntity tile = barrel.getTileEntity();
							if (tile != null) {
								textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
								TileEntityRendererDispatcher.instance.render(tile, barrel.getPos().getX(), barrel.getPos().getY(), barrel.getPos().getZ(), 0);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (i == 0) {
						tessellator.draw();
					}
				}
 			}

			GlStateManager.enableLighting();
		}

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();

		GlStateManager.popMatrix();

		TileEntityRendererDispatcher.staticPlayerX = oldSPX;
		TileEntityRendererDispatcher.staticPlayerY = oldSPY;
		TileEntityRendererDispatcher.staticPlayerZ = oldSPZ;
	}
}
