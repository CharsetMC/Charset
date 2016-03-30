package pl.asie.charset.lib.utils;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Function;
import net.minecraft.client.renderer.VertexBuffer;
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

import pl.asie.charset.lib.ModCharsetLib;

public final class ClientUtils {
	public static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>() {
		public TextureAtlasSprite apply(ResourceLocation location) {
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
		}
	};

	private ClientUtils() {

	}

	private static float getFaceBrightness(EnumFacing facing) {
		switch (facing) {
			case DOWN:
				return 0.5F;
			case UP:
				return 1.0F;
			case NORTH:
			case SOUTH:
				return 0.8F;
			case WEST:
			case EAST:
				return 0.6F;
			default:
				return 1.0F;
		}
	}

	public static int getFaceColor(int color, EnumFacing facing) {
		int c = color & 0xFF000000;
		c |= (int) (((color & 0xFF0000) >> 16) * getFaceBrightness(facing)) << 16;
		c |= (int) (((color & 0x00FF00) >> 8) * getFaceBrightness(facing)) << 8;
		c |= (int) (((color & 0x0000FF) >> 0) * getFaceBrightness(facing)) << 0;
		return c;
	}

	public static BakedQuad clone(BakedQuad quad) {
		return new BakedQuad(quad.getVertexData(), quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
	}

	public static BakedQuad recolorQuad(BakedQuad quad, int color){
		int c = DefaultVertexFormats.BLOCK.getColorOffset() / 4;
		int v = DefaultVertexFormats.BLOCK.getNextOffset() / 4;
		int cc = getFaceColor(color, quad.getFace());
		int[] vertexData = quad.getVertexData();
		for (int i = 0; i < 4; i++) {
			vertexData[v * i + c] = cc;
		}
		return quad;
	}

	public static void addRecoloredQuads(List<BakedQuad> src, int color, List<BakedQuad> target, EnumFacing facing) {
		boolean hasColor = false;
		int col = 0;
		if (facing != null) {
			hasColor = true;
			col = getFaceColor(color, facing);
		}
		for (BakedQuad quad : src) {
			BakedQuad quad1 = clone(quad);
			int c = DefaultVertexFormats.BLOCK.getColorOffset() / 4;
			int v = DefaultVertexFormats.BLOCK.getNextOffset() / 4;
			int cc = hasColor ? col : getFaceColor(color, quad1.getFace());
			int[] vertexData = quad1.getVertexData();
			for (int i = 0; i < 4; i++) {
				vertexData[v * i + c] = cc;
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
