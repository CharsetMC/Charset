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

package pl.asie.charset.lib.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.module.misc.scaffold.ModelScaffold;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class RenderUtils {
	public static final CharsetFaceBakery BAKERY = new CharsetFaceBakery();
	public static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = ModelLoader.defaultTextureGetter();

	public enum AveragingMode {
		FULL,
		H_EDGES_ONLY,
		V_EDGES_ONLY,
	};

	private static RenderItem renderItem;

	private RenderUtils() {

	}

	public static BakedQuad createQuad(Vector3f from, Vector3f to, @Nonnull EnumFacing facing,
									   TextureAtlasSprite sprite, int tintIndex) {
		Vector3f fFrom = new Vector3f(from);
		Vector3f fTo = new Vector3f(to);
		EnumFacing.AxisDirection facingDir = facing.getAxisDirection();
		switch (facing.getAxis()) {
			case X:
				fFrom.x = fTo.x = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.x : from.x;
				break;
			case Y:
				fFrom.y = fTo.y = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.y : from.y;
				break;
			case Z:
				fFrom.z = fTo.z = facingDir == EnumFacing.AxisDirection.POSITIVE ? to.z : from.z;
				break;
		}

		return BAKERY.makeBakedQuad(fFrom, fTo, tintIndex, sprite, facing, ModelRotation.X0_Y0, true);
	}

	public static int getAverageColor(TextureAtlasSprite sprite, AveragingMode mode) {
		int pixelCount = 0;
		int[] data = sprite.getFrameTextureData(0)[0];
		long[] avgColor = new long[3];
		switch (mode) {
			case FULL:
				for (int j = 0; j < sprite.getIconHeight(); j++) {
					for (int i = 0; i < sprite.getIconWidth(); i++) {
						int c = data[j * sprite.getIconWidth() + i];
						if (((c >> 24) & 0xFF) > 0x00) {
							avgColor[0] += (c & 0xFF)*(c & 0xFF);
							avgColor[1] += ((c >> 8) & 0xFF)*((c >> 8) & 0xFF);
							avgColor[2] += ((c >> 16) & 0xFF)*((c >> 16) & 0xFF);
							pixelCount++;
						}
					}
				}
				break;
			case H_EDGES_ONLY:
				for (int j = 0; j < 2; j++) {
					for (int i = 0; i < sprite.getIconHeight(); i++) {
						int c = data[i * sprite.getIconWidth() + (j > 0 ? sprite.getIconWidth() - 1 : 0)];
						if (((c >> 24) & 0xFF) > 0x00) {
							avgColor[0] += (c & 0xFF)*(c & 0xFF);
							avgColor[1] += ((c >> 8) & 0xFF)*((c >> 8) & 0xFF);
							avgColor[2] += ((c >> 16) & 0xFF)*((c >> 16) & 0xFF);
							pixelCount++;
						}
					}
				}
				break;
			case V_EDGES_ONLY:
				for (int j = 0; j < 2; j++) {
					for (int i = 0; i < sprite.getIconWidth(); i++) {
						int c = data[j > 0 ? (data.length - 1 - i) : i];
						if (((c >> 24) & 0xFF) > 0x00) {
							avgColor[0] += (c & 0xFF)*(c & 0xFF);
							avgColor[1] += ((c >> 8) & 0xFF)*((c >> 8) & 0xFF);
							avgColor[2] += ((c >> 16) & 0xFF)*((c >> 16) & 0xFF);
							pixelCount++;
						}
					}
				}
				break;
		}
		int col = 0xFF000000;
		for (int i = 0; i < 3; i++) {
			col |= ((int) Math.min(255, Math.round(Math.sqrt(avgColor[i] / pixelCount))) & 0xFF) << (i*8);
		}
		return col;
	}

	public static BufferedImage getTextureImage(ResourceLocation location) {
		Minecraft mc = Minecraft.getMinecraft();
		/* TextureAtlasSprite sprite = mc.getTextureMapBlocks().getTextureExtry(location.toString());
		if (sprite != null) {
			int[][] dataM = sprite.getFrameTextureData(0);
			if (dataM != null && dataM.length > 0) {
				int[] data = dataM[0];
				BufferedImage image = new BufferedImage(sprite.getIconWidth(), sprite.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
				image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
				return image;
			}
		} */

		try {
			ResourceLocation pngLocation = new ResourceLocation(location.getResourceDomain(), String.format("%s/%s%s", new Object[] {"textures", location.getResourcePath(), ".png"}));
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(pngLocation);
			return TextureUtil.readBufferedImage(resource.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static TextureAtlasSprite getItemSprite(ItemStack stack) {
		if (renderItem == null) {
			renderItem = Minecraft.getMinecraft().getRenderItem();
		}

		return renderItem.getItemModelWithOverrides(stack, null, null).getParticleTexture();
	}

	public static boolean isBuiltInRenderer(World world, ItemStack stack) {
		if (renderItem == null) {
			renderItem = Minecraft.getMinecraft().getRenderItem();
		}

		IBakedModel model = renderItem.getItemModelWithOverrides(stack, world, null);
		return model != null && model.isBuiltInRenderer();
	}

	public static int asMcIntColor(double[] data) {
		int color = ((int)Math.round(data[0] * 255) << 16) | ((int)Math.round(data[1] * 255) << 8) | (int)Math.round(data[2] * 255);
		if (data.length >= 4) {
			color |= ((int)Math.round(data[3] * 255) << 24);
		} else {
			color |= 0xFF000000;
		}
		return color;
	}

	public static void glColor(int color) {
		GlStateManager.color((((color >> 16) & 0xFF) / 255.0f), (((color >> 8) & 0xFF) / 255.0f), ((color & 0xFF) / 255.0f), (((color >> 24) & 0xFF) / 255.0f));
	}

	public static float[] calculateUV(Vector3f from, Vector3f to, EnumFacing facing1) {
		if (to.y > 16) {
			from = new Vector3f(from);
			to = new Vector3f(to);
			while (to.y > 16) {
				from.y -= 16;
				to.y -= 16;
			}
		}

		EnumFacing facing = facing1;
		if (facing == null) {
			if (from.y == to.y) {
				facing = EnumFacing.UP;
			} else if (from.x == to.x) {
				facing = EnumFacing.EAST;
			} else if (from.z == to.z) {
				facing = EnumFacing.SOUTH;
			} else {
				return null; // !?
			}
		}

		switch (facing) {
			case DOWN:
				return new float[] {from.x, 16.0F - to.z, to.x, 16.0F - from.z};
			case UP:
				return new float[] {from.x, from.z, to.x, to.z};
			case NORTH:
				return new float[] {16.0F - to.x, 16.0F - to.y, 16.0F - from.x, 16.0F - from.y};
			case SOUTH:
				return new float[] {from.x, 16.0F - to.y, to.x, 16.0F - from.y};
			case WEST:
				return new float[] {from.z, 16.0F - to.y, to.z, 16.0F - from.y};
			case EAST:
				return new float[] {16.0F - to.z, 16.0F - to.y, 16.0F - from.z, 16.0F - from.y};
		}

		return null;
	}

	public static IModel getModel(ResourceLocation location) {
		try {
			return ModelLoaderRegistry.getModel(location);
		} catch (Exception e) {
			ModCharset.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
			e.printStackTrace();
			return null;
		}
	}

	public static IModel getModelWithTextures(ResourceLocation location, TextureMap map) {
		IModel model = getModel(location);
		if (model != null) {
			for (ResourceLocation tlocation : model.getTextures()) {
				map.registerSprite(tlocation);
			}
		}
		return model;
	}

	private static int getSelectionMask(int y, int x, int z) {
		return 1 << (y * 4 + x * 2 + z);
	}

	private static void drawLine(BufferBuilder worldrenderer, Tessellator tessellator, double x1, double y1, double z1, double x2, double y2, double z2) {
		worldrenderer.pos(x1, y1, z1).endVertex();
		worldrenderer.pos(x2, y2, z2).endVertex();
	}

	public static int getSelectionMask(EnumFacing face) {
		int lineMask = 0;
		switch (face) {
			case DOWN:
				return 0x00F;
			case UP:
				return 0xF00;
			case NORTH:
				lineMask |= getSelectionMask(1, 0, 0);
				lineMask |= getSelectionMask(1, 1, 0);
				lineMask |= getSelectionMask(0, 0, 0);
				lineMask |= getSelectionMask(2, 0, 0);
				return lineMask;
			case SOUTH:
				lineMask |= getSelectionMask(1, 0, 1);
				lineMask |= getSelectionMask(1, 1, 1);
				lineMask |= getSelectionMask(0, 0, 1);
				lineMask |= getSelectionMask(2, 0, 1);
				return lineMask;
			case WEST:
				lineMask |= getSelectionMask(1, 0, 0);
				lineMask |= getSelectionMask(1, 0, 1);
				lineMask |= getSelectionMask(0, 1, 0);
				lineMask |= getSelectionMask(2, 1, 0);
				return lineMask;
			case EAST:
				lineMask |= getSelectionMask(1, 1, 0);
				lineMask |= getSelectionMask(1, 1, 1);
				lineMask |= getSelectionMask(0, 1, 1);
				lineMask |= getSelectionMask(2, 1, 1);
				return lineMask;
		}
		return lineMask;
	}

	public static void drawSelectionBoundingBox(AxisAlignedBB box, int lineMask) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

		double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
		double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
		double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

		AxisAlignedBB boundingBox = box.grow(0.0020000000949949026D).offset(-playerX, -playerY, -playerZ);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		if ((lineMask & getSelectionMask(0, 0, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		}
		if ((lineMask & getSelectionMask(0, 0, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(0, 1, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(0, 1, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(1, 0, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & getSelectionMask(1, 0, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(1, 1, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & getSelectionMask(1, 1, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(2, 0, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & getSelectionMask(2, 0, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(2, 1, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getSelectionMask(2, 1, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static int multiplyColor(int src, int dst) {
		int out = 0;
		for (int i = 0; i < 32; i += 8) {
			out |= ((((src >> i) & 0xFF) * ((dst >> i) & 0xFF) / 0xFF) & 0xFF) << i;
		}
		return out;
	}
}
