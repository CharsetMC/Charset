/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
