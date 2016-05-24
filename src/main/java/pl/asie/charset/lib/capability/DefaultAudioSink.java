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

package pl.asie.charset.lib.capability;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSink;

// TODO: Make this work?
public class DefaultAudioSink implements IAudioSink {
    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public Vec3d getPos() {
        return null;
    }

    @Override
    public float getHearingDistance() {
        return 32.0f;
    }

    @Override
    public float getVolume() {
        return 1.0f;
    }

    @Override
    public boolean receive(AudioPacket packet) {
        return packet.add(this);
    }
}
