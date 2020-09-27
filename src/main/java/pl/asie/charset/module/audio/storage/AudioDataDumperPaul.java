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

package pl.asie.charset.module.audio.storage;

import org.apache.commons.lang3.tuple.Pair;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecIBXM;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AudioDataDumperPaul implements IAudioDataDumper {
    private final ICodec codec;

    public AudioDataDumperPaul(ICodec codec) {
        this.codec = codec;
    }

    @Override
    public Pair<byte[], AudioFormat> getAudioData(long maxSize) {
        SoundBuffer buffer;
        AudioFormat format;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        long dataLength = 0;

        format = codec.getAudioFormat();
        long maxLength = maxSize * format.getSampleSizeInBits() / 8 * format.getChannels() * (int) format.getSampleRate() / 1000;
        while (!codec.endOfStream() && dataLength < maxLength) {
            buffer = codec.read();
            if (buffer == null) {
                return null;
            }

            long dataPartLength = Math.min(maxLength - dataLength, buffer.audioData.length);
            dataLength += dataPartLength;
            data.write(buffer.audioData, 0, (int) dataPartLength);
        }

        return Pair.of(data.toByteArray(), format);
    }

    @Override
    public void initialize(File f) {
        try {
            codec.initialize(f.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean initialized() {
        return codec.initialized();
    }
}
