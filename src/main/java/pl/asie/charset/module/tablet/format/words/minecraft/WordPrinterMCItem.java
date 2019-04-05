/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tablet.format.words.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.module.tablet.format.api.IPrintingContextMinecraft;
import pl.asie.charset.module.tablet.format.api.IStyle;
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.StyleScale;
import pl.asie.charset.module.tablet.format.words.WordItem;

public class WordPrinterMCItem extends WordPrinterMinecraft<WordItem> {
	@Override
	public int getWidth(IPrintingContextMinecraft context, WordItem word) {
		return (int) Math.ceil(StyleScale.get(context.getStyleList()) * 16);
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordItem word) {
		return (int) Math.ceil(StyleScale.get(context.getStyleList()) * 16);
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordItem word, int x, int y, boolean isHovering) {
		ItemStack toDraw = word.getItem();
		RenderItem ri = Minecraft.getMinecraft().getRenderItem();
		float scale = StyleScale.get(context.getStyleList());

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(scale, scale, scale);
		RenderHelper.enableGUIStandardItemLighting();
		ri.renderItemAndEffectIntoGUI(toDraw, 0, 0);
		ri.renderItemOverlayIntoGUI(context.getFontRenderer(), toDraw, 0, 0, null);
		GlStateManager.popMatrix();
	}
}
