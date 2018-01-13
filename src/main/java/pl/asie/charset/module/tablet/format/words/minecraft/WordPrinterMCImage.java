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

package pl.asie.charset.module.tablet.format.words.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.module.tablet.format.api.IPrintingContextMinecraft;
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.WordImage;

public class WordPrinterMCImage extends WordPrinterMinecraft<WordImage> {
	@Override
	public int getWidth(IPrintingContextMinecraft context, WordImage word) {
		return word.width;
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordImage word) {
		return word.height;
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordImage word, int x, int y, boolean isHovering) {
		int z = 0;
		Minecraft.getMinecraft().renderEngine.bindTexture(word.resource); // NORELEASE memleak goes here! :|
		double u0 = 0;
		double v0 = 0;
		double u1 = 1;
		double v1 = 1;
		Tessellator tessI = Tessellator.getInstance();
		BufferBuilder tess = tessI.getBuffer();
		GlStateManager.color(1, 1, 1, 1);
		tess.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		tess.pos(x + 0, y + 0, z         ).tex(u0, v0).endVertex();
		tess.pos(x + 0, y + word.height, z    ).tex(u0, v1).endVertex();
		tess.pos(x + word.width, y + word.height, z).tex(u1, v1).endVertex();
		tess.pos(x + word.width, y + 0, z     ).tex(u1, v0).endVertex();
		tessI.draw();
	}
}
