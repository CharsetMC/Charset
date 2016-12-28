package pl.asie.charset.crafting;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.network.Packet;

import java.io.IOException;
import java.util.Locale;

public class GuiPocketTable extends GuiContainer {
    private static final ResourceLocation POCKET_GUI = new ResourceLocation("charsetcrafting:textures/gui/pocketgui.png");

    public ContainerPocket containerPocket;

    public GuiPocketTable(ContainerPocket container) {
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
        this.fontRendererObj.drawString("PcktCrftng", 178, 10, 4210752);
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
            this.fontRendererObj.drawString(key + ": " + msg, 8, y, color);
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
        // 'x' clears items out of the way. Fill inv, then bag (and make slurp
        // sound). [XXX TODO -- Doing this server-friendly'd require a packet or
        // something]
        // 'z' in crafting area, balance items out. Then fill in with rest of
        // inventory.
        // 'c' cycle layout, based on outer edge:
        // - Full: rotate
        // - In a '\' corner: spread left/right
        // - In a '/' corner: spread up/down
        // - A line along a side: spread to the other side, skipping the middle.
        // - Two touching: fill the circumerfence, alternating.
        // - middle of a side: spread across center
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
