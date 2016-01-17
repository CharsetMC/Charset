package pl.asie.charset.lib.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

/**
 * Created by asie on 1/17/16.
 */
public class GuiContainerCharset extends GuiContainer {
	protected int xCenter, yCenter;

	public GuiContainerCharset(Container container, int xSize, int ySize) {
		super(container);
		this.xSize = xSize;
		this.ySize = ySize;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.xCenter = (this.width - this.xSize) / 2;
		this.yCenter = (this.height - this.ySize) / 2;
	}
}
