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

public abstract class AudioPacket implements Cloneable, IAudioModifierContainer {
    protected Set<AudioSink> sinks = new HashSet<AudioSink>();

    public boolean add(@Nonnull AudioSink sink) {
        return sinks.add(sink);
    }

    public boolean addAll(@Nonnull Collection<AudioSink> sinks) {
        return sinks.addAll(sinks);
    }

    public Set<AudioSink> getSinks() {
        return ImmutableSet.copyOf(sinks);
    }

    public int getSinkCount() {
        return sinks.size();
    }

    public abstract void beginPropagation();
    public abstract void finishPropagation();

    public void writeData(ByteBuf buffer) {
        buffer.writeShort(AudioAPI.PACKET_REGISTRY.getId(this.getClass()));
        buffer.writeShort(sinks.size());
        for (AudioSink sink : sinks) {
            sink.writeData(buffer);
        }
    }

    public void readData(ByteBuf buffer) {
        int sinkLen = buffer.readUnsignedShort();
        sinks.clear();
        for (int i = 0; i < sinkLen; i++) {
            sinks.add(AudioSink.create(buffer));
        }
    }

    public static AudioPacket create(ByteBuf buffer) {
        try {
            AudioPacket packet = AudioAPI.PACKET_REGISTRY.get(buffer.readUnsignedShort()).newInstance();
            packet.readData(buffer);
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
