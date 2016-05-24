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

package pl.asie.charset.lib.utils;

import java.util.List;

import com.google.common.base.Function;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.render.CharsetFaceBakery;

public final class RenderUtils {
	public static final CharsetFaceBakery BAKERY = new CharsetFaceBakery();
	public static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>() {
		public TextureAtlasSprite apply(ResourceLocation location) {
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
		}
	};

	private static RenderItem renderItem;

	private RenderUtils() {

	}

	public static boolean isDynamicItemRenderer(World world, ItemStack stack) {
		if (renderItem == null) {
			renderItem = Minecraft.getMinecraft().getRenderItem();
		}

		IBakedModel model = renderItem.getItemModelWithOverrides(stack, world, null);
		return model != null && model.isBuiltInRenderer();
	}

	public static void glColor(int color) {
		GlStateManager.color((((color >> 16) & 0xFF) / 255.0f), (((color >> 8) & 0xFF) / 255.0f), ((color & 0xFF) / 255.0f));
	}

	public static float[] calculateUV(Vector3f from, Vector3f to, EnumFacing facing1) {
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

	public static BakedQuad clone(BakedQuad quad) {
		return new BakedQuad(quad.getVertexData(), quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
	}

	public static BakedQuad recolorQuad(BakedQuad quad, int color) {
		int c = DefaultVertexFormats.BLOCK.getColorOffset() / 4;
		int v = DefaultVertexFormats.BLOCK.getNextOffset() / 4;
		int[] vertexData = quad.getVertexData();
		for (int i = 0; i < 4; i++) {
			vertexData[v * i + c] = multiplyColor(vertexData[v * i + c], color);
		}
		return quad;
	}

	private static int multiplyColor(int src, int dst) {
		int out = 0;
		for (int i = 0; i < 32; i += 8) {
			out |= ((((src >> i) & 0xFF) * ((dst >> i) & 0xFF) / 0xFF) & 0xFF) << i;
		}
		return out;
	}

	public static void addRecoloredQuads(List<BakedQuad> src, int color, List<BakedQuad> target, EnumFacing facing) {
		for (BakedQuad quad : src) {
			BakedQuad quad1 = clone(quad);
			int c = DefaultVertexFormats.BLOCK.getColorOffset() / 4;
			int v = DefaultVertexFormats.BLOCK.getNextOffset() / 4;
			int[] vertexData = quad1.getVertexData();
			for (int i = 0; i < 4; i++) {
				vertexData[v * i + c] = multiplyColor(vertexData[v * i + c], color);
			}
			target.add(quad1);
		}
	}

	public static IModel getModel(ResourceLocation location) {
		try {
			IModel model = ModelLoaderRegistry.getModel(location);
			if (model == null) {
				ModCharsetLib.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
			}
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int getLineMask(int y, int x, int z) {
		return 1 << (y * 4 + x * 2 + z);
	}

	private static void drawLine(VertexBuffer worldrenderer, Tessellator tessellator, double x1, double y1, double z1, double x2, double y2, double z2) {
		worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		worldrenderer.pos(x1, y1, z1).endVertex();
		worldrenderer.pos(x2, y2, z2).endVertex();
		tessellator.draw();
	}

	public static int getLineMask(EnumFacing face) {
		int lineMask = 0;
		switch (face) {
			case DOWN:
				return 0x00F;
			case UP:
				return 0xF00;
			case NORTH:
				lineMask |= getLineMask(1, 0, 0);
				lineMask |= getLineMask(1, 1, 0);
				lineMask |= getLineMask(0, 0, 0);
				lineMask |= getLineMask(2, 0, 0);
				return lineMask;
			case SOUTH:
				lineMask |= getLineMask(1, 0, 1);
				lineMask |= getLineMask(1, 1, 1);
				lineMask |= getLineMask(0, 0, 1);
				lineMask |= getLineMask(2, 0, 1);
				return lineMask;
			case WEST:
				lineMask |= getLineMask(1, 0, 0);
				lineMask |= getLineMask(1, 0, 1);
				lineMask |= getLineMask(0, 1, 0);
				lineMask |= getLineMask(2, 1, 0);
				return lineMask;
			case EAST:
				lineMask |= getLineMask(1, 1, 0);
				lineMask |= getLineMask(1, 1, 1);
				lineMask |= getLineMask(0, 1, 1);
				lineMask |= getLineMask(2, 1, 1);
				return lineMask;
		}
		return lineMask;
	}

	public static void drawSelectionBoundingBox(AxisAlignedBB box, int lineMask) {
		AxisAlignedBB boundingBox = box.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldrenderer = tessellator.getBuffer();
		if ((lineMask & getLineMask(0, 0, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		}
		if ((lineMask & getLineMask(0, 0, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(0, 1, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(0, 1, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(1, 0, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & getLineMask(1, 0, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(1, 1, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & getLineMask(1, 1, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.minY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(2, 0, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		}
		if ((lineMask & getLineMask(2, 0, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(2, 1, 0)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.minX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		}
		if ((lineMask & getLineMask(2, 1, 1)) != 0) {
			drawLine(worldrenderer, tessellator,
					boundingBox.maxX, boundingBox.maxY, boundingBox.minZ,
					boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		}

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
}
