package pl.asie.charset.module.tools.building.chisel;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

public class PacketSetBlockMask extends Packet {
    private int blockMask, heldPos;

    public PacketSetBlockMask() {

    }

    public PacketSetBlockMask(int blockMask, int heldPos) {
        this.blockMask = blockMask;
        this.heldPos = heldPos;
    }

    @Override
    public void readData(INetHandler handler, ByteBuf buf) {
        blockMask = buf.readUnsignedShort();
        heldPos = buf.readInt();
    }

    @Override
    public void apply(INetHandler handler) {
        EntityPlayer player = getPlayer(handler);
        if (player != null) {
            ItemStack stack = player.inventory.getStackInSlot(heldPos);
            if (stack.getItem() == CharsetToolsBuilding.chisel) {
                CharsetToolsBuilding.chisel.setBlockMask(stack, blockMask);
            }
        }
    }

    @Override
    public void writeData(ByteBuf buf) {
        buf.writeShort(blockMask);
        buf.writeInt(heldPos);
    }

    @Override
    public boolean isAsynchronous() {
        return false;
    }
}
