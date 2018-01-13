package pl.asie.charset.module.tablet.format.words.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.module.tablet.format.api.IPrintingContextMinecraft;
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.WordItem;

public class WordPrinterMCItem extends WordPrinterMinecraft<WordItem> {
	@Override
	public int getWidth(IPrintingContextMinecraft context, WordItem word) {
		return (int) Math.ceil(word.getScale() * 16);
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordItem word) {
		return (int) Math.ceil(word.getScale() * 16);
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordItem word, int x, int y, boolean isHovering) {
		ItemStack toDraw = word.getItem();
		RenderItem ri = Minecraft.getMinecraft().getRenderItem();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(word.getScale(), word.getScale(), word.getScale());
		RenderHelper.enableGUIStandardItemLighting();
		ri.renderItemAndEffectIntoGUI(toDraw, 0, 0);
		ri.renderItemOverlayIntoGUI(context.getFontRenderer(), toDraw, 0, 0, null);
		GlStateManager.popMatrix();
	}
}
