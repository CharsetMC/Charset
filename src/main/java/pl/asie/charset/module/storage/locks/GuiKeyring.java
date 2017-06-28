package pl.asie.charset.module.storage.locks;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.ui.GuiContainerCharset;

/**
 * Created by asie on 6/28/17.
 */
public class GuiKeyring extends GuiContainerCharset {
    private static final ResourceLocation GENERIC_54 = new ResourceLocation("minecraft:textures/gui/container/generic_54.png");

    public GuiKeyring(Container container) {
        super(container, 176, 131);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GENERIC_54);

        drawTexturedModalRect(xCenter, yCenter, 0, 0, xSize, 35);
        drawTexturedModalRect(xCenter, yCenter + 35, 0, 125, xSize, ySize - 35);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(I18n.format("gui.charset.keyring.name"), 8, 6, 4210752);
    }
}
