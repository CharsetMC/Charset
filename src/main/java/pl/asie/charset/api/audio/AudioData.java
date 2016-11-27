package pl.asie.charset.api.audio;

import io.netty.buffer.ByteBuf;

public abstract class AudioData {
    public abstract int getTime();
    public abstract void readData(ByteBuf buf);
    public abstract void writeData(ByteBuf buf);
    protected abstract void sendClient(AudioPacket packet);
}
