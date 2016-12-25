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

package pl.asie.charset.lib.audio.types;

import io.netty.buffer.ByteBuf;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioDataPCM;
import pl.asie.charset.lib.audio.AudioUtils;
import pl.asie.charset.lib.audio.codec.DFPWM;

public class AudioDataDFPWM extends AudioData implements IAudioDataPCM {
    protected byte[] decodedData;
    private byte[] data;
    private int time;
    private static final DFPWM dfpwm = new DFPWM();

    private transient int sourceId;

    public AudioDataDFPWM() {

    }

    public AudioDataDFPWM(byte[] data, int time) {
        this.data = data;
        this.time = time;
    }

    public AudioDataDFPWM setSourceId(int id) {
        this.sourceId = id;
        return this;
    }

    @Override
    public int getSampleRate() {
        int samples = data.length * 8;
        return samples * 1000 / time;
    }

    @Override
    public int getSampleSize() {
        return 1;
    }

    @Override
    public boolean isSampleSigned() {
        return true;
    }

    @Override
    public boolean isSampleBigEndian() {
        return false;
    }

    @Override
    public byte[] getSamplePCMData() {
        if (decodedData == null) {
            decodedData = new byte[data.length * 8];
			dfpwm.decompress(decodedData, data, 0, 0, data.length);
        }

        return decodedData;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void readData(ByteBuf buf) {
        time = buf.readUnsignedMedium();
        data = new byte[buf.readUnsignedShort()];
        buf.readBytes(data);
    }

    @Override
    public void writeData(ByteBuf buf) {
        buf.writeMedium(time);
        buf.writeShort(data.length);
        buf.writeBytes(data);
    }

    @Override
    protected void sendClient(AudioPacket packet) {
        AudioUtils.send(sourceId, packet);
    }
}
