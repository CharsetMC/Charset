package pl.asie.charset.api.audio;

public interface IDataPCM {
    int getSampleRate();
    int getSampleSize(); // in bytes
    boolean isSampleSigned();
    byte[] getSamplePCMData();
}
