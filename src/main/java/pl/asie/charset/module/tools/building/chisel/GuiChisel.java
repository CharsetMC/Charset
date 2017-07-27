/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.tools.building.chisel;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.ui.GuiContainerCharset;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

import java.io.IOException;

public class GuiChisel extends GuiContainerCharset {
    private static final ResourceLocation CHISEL_GUI = new ResourceLocation("charset:textures/gui/chiselgui.png");
    private final ContainerChisel containerChisel;

    public GuiChisel(ContainerChisel container) {
        super(container, 68, 68);
        this.containerChisel = container;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            ItemStack stack = containerChisel.playerInv.getStackInSlot(containerChisel.heldPos);
            if (stack.getItem() == CharsetToolsBuilding.chisel) {
                int blockMask = CharsetToolsBuilding.chisel.getBlockMask(stack);
                int i = 0;

                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        if (insideRect(mouseX, mouseY, xCenter + 8 + x * 18, yCenter + 8 + y * 18, 18, 18)) {
                            blockMask ^= (1 << i);
                        }

                        i++;
                    }
                }

                CharsetToolsBuilding.packet.sendToServer(new PacketSetBlockMask(blockMask, containerChisel.heldPos));
                CharsetToolsBuilding.chisel.setBlockMask(stack, blockMask);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partial, mouseX, mouseY);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHISEL_GUI);

        drawTexturedModalRect(xCenter, yCenter, 0, 0, xSize, ySize);
        ItemStack stack = containerChisel.playerInv.getStackInSlot(containerChisel.heldPos);
        if (stack.getItem() == CharsetToolsBuilding.chisel) {
            GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
            int blockMask = CharsetToolsBuilding.chisel.getBlockMask(stack);
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if ((blockMask & 1) == 0) {
                        drawTexturedModalRect(xCenter + 8 + x*18, yCenter + 8 + y*18,
                                8,8, 16, 16);
                    }
                    blockMask >>= 1;
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
