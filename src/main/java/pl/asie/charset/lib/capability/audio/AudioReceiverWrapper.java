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

package pl.asie.charset.lib.capability.audio;

import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioReceiver;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class AudioReceiverWrapper implements Function<List<IAudioReceiver>, IAudioReceiver> {
    @Override
    public IAudioReceiver apply(List<IAudioReceiver> iAudioReceivers) {
        return new WrappedReceiver(iAudioReceivers);
    }

    private class WrappedReceiver implements IAudioReceiver {
        private final Collection<IAudioReceiver> receivers;

        WrappedReceiver(Collection<IAudioReceiver> receivers) {
            this.receivers = receivers;
        }

        @Override
        public boolean receive(AudioPacket packet) {
            boolean received = false;
            for (IAudioReceiver receiver : receivers) {
                received |= receiver.receive(packet);
            }
            return received;
        }
    }
}
