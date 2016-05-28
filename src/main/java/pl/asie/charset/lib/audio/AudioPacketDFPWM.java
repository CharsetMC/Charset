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

import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.lib.utils.DFPWM;

public class AudioPacketDFPWM extends AudioPacketCharset {
    protected byte[] decodedData;
    private static final DFPWM dfpwm = new DFPWM();

    public AudioPacketDFPWM() {

    }

    public AudioPacketDFPWM(int id, byte[] data, int time) {
        super(id, data, time);
    }

    @Override
    public AudioPacket clone() {
        return new AudioPacketDFPWM(id, data, time);
    }

    @Override
    public int getPCMSampleRate() {
        int samples = data.length * 8;
        return samples * 1000 / time;
    }

    @Override
    public int getPCMSampleSizeBits() {
        return 8;
    }

    @Override
    public boolean getPCMSigned() {
        return true;
    }

    @Override
    public byte[] getPCMData() {
        if (decodedData == null) {
            decodedData = new byte[data.length * 8];
			dfpwm.decompress(decodedData, data, 0, 0, data.length);
        }

        return decodedData;
    }
}
