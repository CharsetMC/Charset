package pl.asie.charset.module.audio.microphone;

import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;

import java.util.UUID;

public interface IWirelessAudioReceiver {
	void receiveWireless(UUID senderId, AudioData packet);
}
