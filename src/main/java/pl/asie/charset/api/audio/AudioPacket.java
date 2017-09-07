/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import pl.asie.charset.api.CharsetAPI;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class AudioPacket {
    protected Set<AudioSink> sinks = new HashSet<AudioSink>();
    protected AudioData data;
    protected float volume;

    public AudioPacket(AudioPacket parent) {
        this(parent.getData(), parent.getVolume());
    }

    public AudioPacket(AudioData data, float volume) {
        this.data = data;
        this.volume = volume;
    }

    public AudioPacket() {

    }

    public void send() {
        data.sendClient(this);
    }

    public float getVolume() {
        return volume;
    }

    public boolean add(@Nonnull AudioSink sink) {
        return sinks.add(sink);
    }

    public boolean addAll(@Nonnull Collection<AudioSink> sinks) {
        return this.sinks.addAll(sinks);
    }

    public AudioData getData() {
        return data;
    }

    public Set<AudioSink> getSinks() {
        return ImmutableSet.copyOf(sinks);
    }

    public int getSinkCount() {
        return sinks.size();
    }

    public void writeData(ByteBuf buffer) {
        buffer.writeFloat(volume);
        buffer.writeShort(CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).getId(data));
        data.writeData(buffer);
        buffer.writeShort(sinks.size());
        for (AudioSink sink : sinks) {
            sink.writeData(buffer);
        }
    }

    public void readData(ByteBuf buffer) {
        volume = buffer.readFloat();
        data = CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).create(buffer.readUnsignedShort());
        data.readData(buffer);
        int sinkLen = buffer.readUnsignedShort();
        sinks.clear();
        for (int i = 0; i < sinkLen; i++) {
            sinks.add(AudioSink.create(buffer));
        }
    }
}
