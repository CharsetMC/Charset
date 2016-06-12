package pl.asie.charset.api.audio;

import io.netty.buffer.ByteBuf;


public abstract class AudioData {
    public abstract int getTime(); // in milliseconds
    public abstract int getSize();
    public abstract void readData(ByteBuf buf);
    public abstract void writeData(ByteBuf buf);
}
