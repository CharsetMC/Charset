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

package pl.asie.charset.module.tools.building.chisel;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.inventory.GuiContainerCharset;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

import java.io.IOException;

public class GuiChisel extends GuiContainerCharset<ContainerChisel> {
    private static final ResourceLocation CHISEL_GUI = new ResourceLocation("charset:textures/gui/chiselgui.png");

    public GuiChisel(ContainerChisel container) {
        super(container, 68, 68);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            ItemStack stack = container.playerInv.getStackInSlot(container.heldPos);
            if (stack.getItem() == CharsetToolsBuilding.chisel) {
                int blockMask = CharsetToolsBuilding.chisel.getBlockMask(stack);
                int i = 0;

                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        if (insideRect(mouseX, mouseY, xBase + 8 + x * 18, yBase + 8 + y * 18, 18, 18)) {
                            blockMask ^= (1 << i);
                        }

                        i++;
                    }
                }

                CharsetToolsBuilding.packet.sendToServer(new PacketSetBlockMask(blockMask, container.heldPos));
                CharsetToolsBuilding.chisel.setBlockMask(stack, blockMask);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partial, mouseX, mouseY);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHISEL_GUI);

        drawTexturedModalRect(xBase, yBase, 0, 0, xSize, ySize);
        ItemStack stack = container.playerInv.getStackInSlot(container.heldPos);
        if (stack.getItem() == CharsetToolsBuilding.chisel) {
            GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
            int blockMask = CharsetToolsBuilding.chisel.getBlockMask(stack);
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if ((blockMask & 1) == 0) {
                        drawTexturedModalRect(xBase + 8 + x*18, yBase + 8 + y*18,
                                8,8, 16, 16);
                    }
                    blockMask >>= 1;
                }
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
