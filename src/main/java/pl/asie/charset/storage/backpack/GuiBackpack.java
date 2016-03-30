package pl.asie.charset.storage.backpack;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiBackpack extends GuiContainer {
	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
	private ContainerBackpack container;

	public GuiBackpack(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		this.container = (ContainerBackpack) inventorySlotsIn;
		this.allowUserInput = false;
		this.ySize = 168;
	}

	public ITextComponent getDisplayName() {
		return new TextComponentTranslation("tile.charset.backpack.name");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(getDisplayName().getUnformattedText(), 8, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, 3 * 18 + 17);
		this.drawTexturedModalRect(i, j + 3 * 18 + 17, 0, 126, this.xSize, 96);
	}
}
