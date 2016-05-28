package pl.asie.charset.api.audio;

public interface IPCMPacket {
    int getPCMSampleRate();
    int getPCMSampleSizeBits();
    boolean getPCMSigned();
    byte[] getPCMData();
}
