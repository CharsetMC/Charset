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
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.lib.audio.AudioUtils;

public class AudioDataGameSound extends AudioData implements IDataGameSound {
    private String name;
    private float pitch;

    public AudioDataGameSound() {

    }

    public AudioDataGameSound(String name, float pitch) {
        this.name = name;
        this.pitch = pitch;
    }

    @Override
    public int getTime() {
        return 10;
    }

    @Override
    public void readData(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        pitch = buf.readFloat();
    }

    @Override
    public void writeData(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeFloat(pitch);
    }

    @Override
    protected void sendClient(AudioPacket packet) {
        AudioUtils.send(0, packet);
    }

    @Override
    public String getSoundName() {
        return name;
    }

    @Override
    public float getSoundPitch() {
        return pitch;
    }
}
