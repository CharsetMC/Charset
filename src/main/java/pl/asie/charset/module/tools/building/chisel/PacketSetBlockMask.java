/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
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
    public void readData(INetHandler handler, PacketBuffer buf) {
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
    public void writeData(PacketBuffer buf) {
        buf.writeShort(blockMask);
        buf.writeInt(heldPos);
    }

    @Override
    public boolean isAsynchronous() {
        return false;
    }
}
