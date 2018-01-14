/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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
