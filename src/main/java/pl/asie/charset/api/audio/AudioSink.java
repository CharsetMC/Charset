/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.api.audio;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.api.CharsetAPI;

public abstract class AudioSink extends IForgeRegistryEntry.Impl<AudioSink> implements IAudioReceiver {
    public abstract World getWorld();
    public abstract Vec3d getPos();
    public abstract float getDistance();
    public abstract float getVolume();

    @Override
    public boolean receive(AudioPacket packet) {
        return packet.add(this);
    }

    public void writeData(ByteBuf buffer) {
        buffer.writeShort(CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioSink.class).getId(this));
    }

    public void readData(ByteBuf buffer) {

    }

    public static AudioSink create(ByteBuf buffer) {
        try {
            AudioSink sink = CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioSink.class).create(buffer.readUnsignedShort());
            sink.readData(buffer);
            return sink;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
