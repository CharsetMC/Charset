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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IDataPCM;
import pl.asie.charset.lib.audio.codec.DFPWM;

public class AudioDataSound extends AudioData implements IDataSound {
    private String name;
    private float pitch;

    public AudioDataSound() {

    }

    public AudioDataSound(String name, float pitch) {
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
