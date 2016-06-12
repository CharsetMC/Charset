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

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;

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

    public float getVolume() {
        return volume;
    }

    public boolean add(@Nonnull AudioSink sink) {
        return sinks.add(sink);
    }

    public boolean addAll(@Nonnull Collection<AudioSink> sinks) {
        return sinks.addAll(sinks);
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
        buffer.writeShort(AudioAPI.DATA_REGISTRY.getId(data.getClass()));
        data.writeData(buffer);
        buffer.writeShort(sinks.size());
        for (AudioSink sink : sinks) {
            sink.writeData(buffer);
        }
    }

    public void readData(ByteBuf buffer) {
        volume = buffer.readFloat();
        data = AudioAPI.DATA_REGISTRY.create(buffer.readUnsignedShort());
        data.readData(buffer);
        int sinkLen = buffer.readUnsignedShort();
        sinks.clear();
        for (int i = 0; i < sinkLen; i++) {
            sinks.add(AudioSink.create(buffer));
        }
    }
}
