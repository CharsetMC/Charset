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

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;

public final class AudioUtils {
    private AudioUtils() {

    }

    public static int start() {
        return AudioStreamManager.INSTANCE.create();
    }

    public static void stop(int id) {
        ModCharsetLib.packet.sendToAll(new PacketAudioStop(id));
        AudioStreamManager.INSTANCE.remove(id);
    }
}
