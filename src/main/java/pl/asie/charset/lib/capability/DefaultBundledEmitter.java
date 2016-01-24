package pl.asie.charset.lib.capability;

import pl.asie.charset.api.wires.IBundledEmitter;

public class DefaultBundledEmitter implements IBundledEmitter {
	private byte[] data;

	public DefaultBundledEmitter(byte[] data) {
		this.data = data;
	}

	public DefaultBundledEmitter() {
		this.data = new byte[16];
	}

	@Override
	public byte[] getBundledSignal() {
		return data;
	}

	public void emit(byte[] data) {
		if (data == null || data.length != 16) {
			data = new byte[16];
		} else {
			this.data = data;
		}
	}
}
