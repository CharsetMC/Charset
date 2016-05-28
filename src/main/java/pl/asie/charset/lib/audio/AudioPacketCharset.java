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

package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.api.audio.IPCMPacket;
import pl.asie.charset.lib.ModCharsetLib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AudioPacketCharset extends AudioPacket implements IPCMPacket {
    protected int id;
    protected byte[] data;
    protected int time;

    public AudioPacketCharset() {

    }

    public AudioPacketCharset(int id, byte[] data, int time) {
        this.id = id;
        this.data = data;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void readData(ByteBuf buf) {
        super.readData(buf);
        id = buf.readInt();
        time = buf.readMedium();
        data = new byte[buf.readUnsignedShort()];
        buf.readBytes(data);
    }

    @Override
    public void writeData(ByteBuf buf) {
        super.writeData(buf);
        buf.writeInt(id);
        buf.writeMedium(time);
        buf.writeShort(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void finishPropagation() {
        PacketAudioData packet = new PacketAudioData(this);

        Map<WorldServer, Set<AudioSink>> worlds = new HashMap<>();
        for (AudioSink sink : sinks) {
            if (sink.getVolume() <= 0.0f || sink.getHearingDistance() <= 0.0f) {
                continue;
            }

            if (worlds.containsKey(sink.getWorld())) {
                worlds.get(sink.getWorld()).add(sink);
            } else {
                HashSet<AudioSink> sinkLocal = new HashSet<>();
                sinkLocal.add(sink);
                worlds.put((WorldServer) sink.getWorld(), sinkLocal);
            }
        }

        for (WorldServer world : worlds.keySet()) {
            for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayerList()) {
                if (player.worldObj.provider.getDimension() == world.provider.getDimension()) {
                    for (AudioSink sink : worlds.get(world)) {
                       BlockPos pos = new BlockPos(sink.getPos());
                        if (world.getPlayerChunkMap().isPlayerWatchingChunk(player, pos.getX() >> 4, pos.getZ() >> 4)) {
                            ModCharsetLib.packet.sendTo(packet, player);
                            break;
                        }
                    }
                }
            }
        }
    }
}
