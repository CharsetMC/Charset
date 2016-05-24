package pl.asie.charset.lib.multipart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fmp.client.multipart.MultipartSpecialRenderer;
import net.minecraftforge.fmp.multipart.IMultipart;
import org.lwjgl.opengl.GL11;

public abstract class MultipartSpecialRendererBase<T extends IMultipart> extends MultipartSpecialRenderer<T> {
    protected final Minecraft mc = Minecraft.getMinecraft();

    protected void renderMultipartFastFromSlow(T part, double x, double y, double z, float partialTicks, int destroyStage) {
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        VertexBuffer buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        renderMultipartFast(part, x, y, z, partialTicks, destroyStage, buffer);
        Tessellator.getInstance().draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
