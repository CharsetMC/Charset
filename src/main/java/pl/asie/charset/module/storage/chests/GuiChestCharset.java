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

package pl.asie.charset.module.storage.chests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.ui.GuiContainerCharset;

import java.io.FileNotFoundException;
import java.io.IOException;

public class GuiChestCharset extends GuiContainerCharset {
	private ResourceLocation texture;

	protected ResourceLocation getTexture() {
		if (texture != null) {
			return texture;
		}

		String s = "textures/gui/container/generic_";
		ResourceLocation first = new ResourceLocation(s + (containerChestCharset.inventoryRows * 9) + ".png");
		try {
			this.mc.getResourceManager().getResource(first);
			texture = first;
		} catch (IOException e) {
			first = new ResourceLocation("charset", s + (containerChestCharset.inventoryRows * 9) + ".png");
			try {
				this.mc.getResourceManager().getResource(first);
				texture = first;
			} catch (IOException ee) {
				texture = new ResourceLocation(s + "54.png");
			}
		}

		return texture;
	}

	private final ContainerChestCharset containerChestCharset;

	public GuiChestCharset(ContainerChestCharset container) {
		super(container, 222, 114 + container.inventoryRows * 18);
		this.containerChestCharset = container;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		if (this.containerChestCharset.tile.getDisplayName() != null) {
			this.fontRenderer.drawString(this.containerChestCharset.tile.getDisplayName().getUnformattedText(), 8, 6, 0x404040);
		}
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

		GlStateManager.color(1, 1, 1,1 );
		this.mc.getTextureManager().bindTexture(getTexture());
		this.drawTexturedModalRect(xBase, yBase, 0, 0, xSize, containerChestCharset.inventoryRows * 18 + 17);
		this.drawTexturedModalRect(xBase, yBase + containerChestCharset.inventoryRows * 18 + 17, 0, 126, xSize, 96);
	}
}
