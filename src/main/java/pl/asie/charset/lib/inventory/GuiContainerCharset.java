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

package pl.asie.charset.lib.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.utils.RenderUtils;

public class GuiContainerCharset<T extends ContainerBase> extends GuiContainer {
	protected int xBase, yBase;
	protected final T container;

	public GuiContainerCharset(T container, int xSize, int ySize) {
		super(container);
		this.xSize = xSize;
		this.ySize = ySize;
		this.container = container;
	}

	protected boolean showPlayerInventoryName() {
		return true;
	}

	protected final boolean insideRect(int x, int y, int x0, int y0, int w, int h) {
		return x >= x0 && y >= y0 && x < (x0 + w) && y < (y0 + h);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.xBase = (this.width - this.xSize) / 2;
		this.yBase = (this.height - this.ySize) / 2;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		if (showPlayerInventoryName() && container.playerInventory != null) {
			this.fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(),
					container.playerInventoryX, container.playerInventoryY - 11,
					0x404040);
		}
	}
}
