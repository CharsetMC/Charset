package pl.asie.charset.api.audio;

public interface IAudioDataPCM {
    int getSampleRate();
    int getSampleSize(); // in bytes
    boolean isSampleSigned();
    boolean isSampleBigEndian();
    byte[] getSamplePCMData();
}
