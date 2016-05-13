package pl.asie.charset.lib.audio;

public class AudioPacketDFPWM extends AudioPacketCharset {
    public AudioPacketDFPWM(int id, byte[] data, int time) {
        super(id, data, time);
    }

    @Override
    public PacketAudioData.Codec getCodec() {
        return PacketAudioData.Codec.DFPWM;
    }
}
