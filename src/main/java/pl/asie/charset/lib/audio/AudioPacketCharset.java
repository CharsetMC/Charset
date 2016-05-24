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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSink;
import pl.asie.charset.lib.ModCharsetLib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AudioPacketCharset extends AudioPacket {
    protected final int id;
    protected final byte[] data;
    protected final int time;

    public AudioPacketCharset(int id, byte[] data, int time) {
        this.id = id;
        this.data = data;
        this.time = time;
    }

    public abstract PacketAudioData.Codec getCodec();

    @Override
    public void beginPropagation() {

    }

    @Override
    public void endPropagation() {
        PacketAudioData packet = new PacketAudioData(id, getCodec(), sinks, data, time);

        Map<WorldServer, Set<IAudioSink>> worlds = new HashMap<>();
        for (IAudioSink sink : sinks) {
            if (sink.getVolume() <= 0.0f || sink.getHearingDistance() <= 0.0f) {
                continue;
            }

            if (worlds.containsKey(sink.getWorld())) {
                worlds.get(sink.getWorld()).add(sink);
            } else {
                HashSet<IAudioSink> sinkLocal = new HashSet<>();
                sinkLocal.add(sink);
                worlds.put((WorldServer) sink.getWorld(), sinkLocal);
            }
        }

        for (WorldServer world : worlds.keySet()) {
            for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayerList()) {
                if (player.worldObj.provider.getDimension() == world.provider.getDimension()) {
                    for (IAudioSink sink : worlds.get(world)) {
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
