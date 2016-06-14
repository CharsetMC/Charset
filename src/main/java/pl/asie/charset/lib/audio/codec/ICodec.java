package pl.asie.charset.lib.audio.codec;

public interface ICodec {
    void compress(byte[] dest, byte[] src, int destoffs, int srcoffs, int len);
    void decompress(byte[] dest, byte[] src, int destoffs, int srcoffs, int len);
}
