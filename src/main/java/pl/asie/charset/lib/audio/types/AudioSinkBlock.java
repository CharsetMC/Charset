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

package pl.asie.charset.lib.audio.types;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.lib.utils.Utils;

public class AudioSinkBlock extends AudioSink {
    private World world;
    private Vec3d pos;

    public AudioSinkBlock() {

    }

    public AudioSinkBlock(World world, BlockPos pos) {
        this.world = world;
        this.pos = new Vec3d(pos);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vec3d getPos() {
        return pos;
    }

    @Override
    public float getDistance() {
        return 32.0F;
    }

    @Override
    public float getVolume() {
        return 1.0F;
    }

    @Override
    public void writeData(ByteBuf buf) {
        super.writeData(buf);
        buf.writeInt(world.provider.getDimension());
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
    }

    @Override
    public void readData(ByteBuf buf) {
        super.readData(buf);
        int dimId = buf.readInt();
        double xPos = buf.readDouble();
        double yPos = buf.readDouble();
        double zPos = buf.readDouble();

        world = Utils.getLocalWorld(dimId);
        pos = new Vec3d(xPos, yPos, zPos);
    }
}
