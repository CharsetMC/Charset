package pl.asie.charset.audio;

import pl.asie.charset.audio.manager.AudioStreamManager;

public class ProxyClient extends ProxyCommon {
	public static AudioStreamManager stream;

	@Override
	public void init() {
		stream = new AudioStreamManager();
	}

	@Override
	public void onServerStop() {
		stream.removeAll();
	}
}
