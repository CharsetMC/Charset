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
import net.minecraftforge.registries.ForgeRegistry;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AudioUtils {
    private AudioUtils() {

    }

    public static void send(int id, AudioPacket audio) {
        if (audio.getVolume() <= 0.0f) {
            return;
        }

        PacketAudioData packet = new PacketAudioData(id, audio);

        Map<WorldServer, Set<AudioSink>> worlds = new HashMap<>();
        for (AudioSink sink : audio.getSinks()) {
            if (sink.getVolume() <= 0.0f || sink.getDistance() <= 0.0f) {
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
            for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
                if (player.world.provider.getDimension() == world.provider.getDimension()) {
                    for (AudioSink sink : worlds.get(world)) {
                        BlockPos pos = new BlockPos(sink.getPos());
                        if (world.getPlayerChunkMap().isPlayerWatchingChunk(player, pos.getX() >> 4, pos.getZ() >> 4)) {
                            CharsetLib.packet.sendTo(packet, player);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static int start() {
        return AudioStreamManager.INSTANCE.create();
    }

    public static void stop(int id) {
        CharsetLib.packet.sendToAll(new PacketAudioStop(id));
        AudioStreamManager.INSTANCE.remove(id);
    }
}
