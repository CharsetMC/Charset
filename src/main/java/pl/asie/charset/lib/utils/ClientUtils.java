package pl.asie.charset.lib.utils;

import java.io.IOException;

import com.google.common.base.Function;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import pl.asie.charset.lib.ModCharsetLib;

public final class ClientUtils {
    public static final Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
    {
        public TextureAtlasSprite apply(ResourceLocation location)
        {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
        }
    };

    private ClientUtils() {

    }

    public static IModel getModel(ResourceLocation location) {
        try {
            IModel model = ModelLoaderRegistry.getModel(location);
            if (model == null) {
                ModCharsetLib.logger.error("Model " + location.toString() + " is missing! THIS WILL CAUSE A CRASH!");
            }
            return model;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getLineMask(int y, int x, int z) {
        return 1 << (y * 4 + x * 2 + z);
    }

    private static void drawLine(WorldRenderer worldrenderer, Tessellator tessellator, double x1, double y1, double z1, double x2, double y2, double z2)
    {
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

    public static void drawSelectionBoundingBox(AxisAlignedBB box, int lineMask)
    {
        AxisAlignedBB boundingBox = box.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
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
