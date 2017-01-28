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

package pl.asie.charset.crafting.pocket;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.crafting.ModCharsetCrafting;

import java.io.IOException;
import java.util.Locale;

public class GuiPocketTable extends GuiContainer {
    private static final ResourceLocation POCKET_GUI = new ResourceLocation("charsetcrafting:textures/gui/pocketgui.png");

    public ContainerPocketTable containerPocket;

    public GuiPocketTable(ContainerPocketTable container) {
        super(container);
        containerPocket = container;
        xSize = 236;
        ySize = 89;
    }

    private int open_time = 0;

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(POCKET_GUI);

        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        open_time++;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // I'd like it to say "Pocket Crafting", but that doesn't fit.
        // Could also make the tab a bit longer...
        // this.fontRenderer.drawString("Crafting", 178, 10, 4210752);
        this.fontRenderer.drawString("PcktCrftng", 178, 10, 4210752);
        int color = 0xa0a0a0;
        for (int i = 0; i < ModCharsetCrafting.pocketActions.length(); i++) {
            char key = ModCharsetCrafting.pocketActions.charAt(i);
            String msg = null;
            switch (i) {
            case 0: msg = "Empty the crafting grid"; break;
            case 1: msg = "Swirl items ↷"; break;
            case 2: msg = "Balance items"; break;
            case 3: msg = "Fill grid with item under cursor"; break;
            }
            if (msg == null) {
                continue;
            }
            int d = 10;
            int y = -d*ModCharsetCrafting.pocketActions.length() + d*i;
            this.fontRenderer.drawString(key + ": " + msg, 8, y, color);
        }
        // this.fontRenderer.drawString("123456789", 178, 10, 4210752);
        // we can fit only that much
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

        if (my_key == ModCharsetCrafting.pocketActions.charAt(0) /* x */) {
            action = PacketPTAction.CLEAR;
        } else if (my_key == ModCharsetCrafting.pocketActions.charAt(1) /* c */) {
            action = PacketPTAction.SWIRL;
        } else if (my_key == ModCharsetCrafting.pocketActions.charAt(2) /* b */) {
            action = PacketPTAction.BALANCE;
        } else if (my_key == ModCharsetCrafting.pocketActions.charAt(3) /* f */) {
            Slot slot = getSlotUnderMouse();
            if (slot != null) {
                action = PacketPTAction.FILL;
                arg = slot.getSlotIndex();
            }
        }

        if (action != -1) {
            ModCharsetCrafting.packet.sendToServer(new PacketPTAction(action, arg));
        } else {
            super.keyTyped(key, par2);
        }
    }
}
