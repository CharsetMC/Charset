/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.block;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.UtilProxyCommon;
import pl.asie.charset.lib.utils.Utils;

import java.util.Random;

public class PacketCustomBlockDust extends Packet {
    public static final Random rand = new Random();
    
    private World world;
    private BlockPos pos;
    private float posX, posY, posZ;
    private int numberOfParticles, dim;
    private float particleSpeed;

    public PacketCustomBlockDust() {

    }

    public PacketCustomBlockDust(World world, BlockPos pos, double posX, double posY, double posZ, int numberOfParticles, float particleSpeed) {
        this.world = world;
        this.pos = pos;
        this.posX = (float) posX;
        this.posY = (float) posY;
        this.posZ = (float) posZ;
        this.numberOfParticles = numberOfParticles;
        this.particleSpeed = particleSpeed;
    }

    @Override
    public void readData(INetHandler handler, PacketBuffer buf) {
        dim = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        this.numberOfParticles = buf.readInt();
        this.posX = buf.readFloat();
        this.posY = buf.readFloat();
        this.posZ = buf.readFloat();
        this.particleSpeed = buf.readFloat();

        this.pos = new BlockPos(x, y, z);
    }

    @Override
    public void apply(INetHandler handler) {
        this.world = getWorld(handler, dim);
        if (world != null) {
            UtilProxyCommon.proxy.spawnBlockDustClient(world, pos, rand, posX, posY, posZ, numberOfParticles, particleSpeed, EnumFacing.UP);
        }
    }

    @Override
    public void writeData(PacketBuffer buf) {
        buf.writeInt(world.provider.getDimension());
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(numberOfParticles);
        buf.writeFloat(posX);
        buf.writeFloat(posY);
        buf.writeFloat(posZ);
        buf.writeFloat(particleSpeed);
    }

    @Override
    public boolean isAsynchronous() {
        return false;
    }
}
