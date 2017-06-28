/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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

package pl.asie.charset.module.misc.pocketcraft;

import com.sun.org.apache.xml.internal.security.utils.I18n;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.ui.GuiContainerCharset;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

public class GuiPocketTable extends GuiContainerCharset {
    private static final ResourceLocation POCKET_GUI = new ResourceLocation("charset:textures/gui/pocketgui.png");
    private final int buttonGridX, buttonGridY, buttonGridCount;

    public ContainerPocketTable containerPocket;

    public GuiPocketTable(ContainerPocketTable container) {
        super(container, 235, 90);
        containerPocket = container;
        buttonGridX = 196;
        buttonGridY = 51;
        buttonGridCount = 3;
    }

    private int open_time = 0;
    private int button_pressed = -1;

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        super.renderHoveredToolTip(mouseX, mouseY);
        for (int i = 0; i < buttonGridCount; i++) {
            int x = xCenter + buttonGridX + (i * 11);
            int y = yCenter + buttonGridY;
            if (insideRect(mouseX, mouseY, x, y, 11, 11) && button_pressed < 0) {
                drawHoveringText(Collections.singletonList(getActionDescription(i)), mouseX, mouseY, fontRenderer);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            for (int i = 0; i < buttonGridCount; i++) {
                int x = xCenter + buttonGridX + (i * 11);
                int y = yCenter + buttonGridY;
                if (insideRect(mouseX, mouseY, x, y, 11, 11) && button_pressed < 0) {
                    button_pressed = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (button_pressed >= 0) {
            switch (button_pressed) {
                case 0: CharsetMiscPocketcraft.packet.sendToServer(new PacketPTAction(PacketPTAction.CLEAR, 0)); break;
                case 1: CharsetMiscPocketcraft.packet.sendToServer(new PacketPTAction(PacketPTAction.SWIRL, 0)); break;
                case 2: CharsetMiscPocketcraft.packet.sendToServer(new PacketPTAction(PacketPTAction.BALANCE, 0)); break;
            }
            button_pressed = -1;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partial, mouseX, mouseY);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(POCKET_GUI);

        drawTexturedModalRect(xCenter, yCenter, 0, 0, xSize, ySize);

        for (int i = 0; i < buttonGridCount; i++) {
            int x = xCenter + buttonGridX + (i * 11);
            int y = yCenter + buttonGridY;
            int state = 0;
            if (button_pressed == i) {
                state = 2;
            } else if (insideRect(mouseX, mouseY, x, y, 11, 11)) {
                state = 1;
            }
            drawTexturedModalRect(x, y, i * 11, ySize + (state * 11), 11, 11);
        }

        open_time++;
    }

    private String getActionDescription(int i) {
        String msg = null;
        switch (i) {
            case 0: msg = "Empty grid"; break;
            case 1: msg = "Swirl items ↷"; break;
            case 2: msg = "Balance items"; break;
            case 3: msg = "Fill grid with item"; break;
        }
        return "[" + Character.toUpperCase(CharsetMiscPocketcraft.pocketActions.charAt(i)) + "] " + msg;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // I'd like it to say "Pocket Crafting", but that doesn't fit.
        // Could also make the tab a bit longer...
        // this.fontRenderer.drawString("Crafting", 178, 10, 4210752);
        this.fontRenderer.drawString(I18n.translate("gui.charset.crafting.name"), 184, 11, 4210752);
        /* int color = 0xa0a0a0;
        int length = 3;
        int d = 10;
        for (int i = 0; i < length; i++) {
            char key = CharsetMiscPocketcraft.pocketActions.charAt(i);
            String msg = getActionDescription(i);
            if (msg == null) {
                continue;
            }
            int y = d * (i - length);
            this.fontRenderer.drawString(key + ": " + msg, 8, y, color);
        }
        // this.fontRenderer.drawString("123456789", 178, 10, 4210752);
        // we can fit only that much */
    }

    @Override
    protected void keyTyped(char key, int par2) throws IOException {
        if (open_time < 4) {
            super.keyTyped(key, par2);
            return;
        }

        char my_key = ("" + key).toLowerCase(Locale.ROOT).charAt(0);
        int action = -1;
        int arg = 0;

        if (my_key == CharsetMiscPocketcraft.pocketActions.charAt(0)) {
            action = PacketPTAction.CLEAR;
        } else if (my_key == CharsetMiscPocketcraft.pocketActions.charAt(1)) {
            action = PacketPTAction.SWIRL;
        } else if (my_key == CharsetMiscPocketcraft.pocketActions.charAt(2)) {
            action = PacketPTAction.BALANCE;
        }/* else if (my_key == CharsetMiscPocketcraft.pocketActions.charAt(3)) {
            Slot slot = getSlotUnderMouse();
            if (slot != null) {
                action = PacketPTAction.FILL;
                arg = slot.getSlotIndex();
            }
        } */

        if (action != -1) {
            CharsetMiscPocketcraft.packet.sendToServer(new PacketPTAction(action, arg));
        } else {
            super.keyTyped(key, par2);
        }
    }
}
