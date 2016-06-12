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

package pl.asie.charset.api.audio;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AudioSink {
    public abstract World getWorld();
    public abstract Vec3d getPos();
    public abstract float getDistance();
    public abstract float getVolume();

    public boolean receive(AudioPacket packet) {
        return packet.add(this);
    }

    public void writeData(ByteBuf buffer) {
        buffer.writeShort(AudioAPI.SINK_REGISTRY.getId(this.getClass()));
    }

    public void readData(ByteBuf buffer) {

    }

    public static AudioSink create(ByteBuf buffer) {
        try {
            AudioSink sink = AudioAPI.SINK_REGISTRY.get(buffer.readUnsignedShort()).newInstance();
            sink.readData(buffer);
            return sink;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
