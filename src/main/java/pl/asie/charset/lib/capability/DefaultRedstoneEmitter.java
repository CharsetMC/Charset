package pl.asie.charset.lib.capability;

import pl.asie.charset.api.wires.IRedstoneEmitter;

public class DefaultRedstoneEmitter implements IRedstoneEmitter {
	private int data;

	public DefaultRedstoneEmitter(int data) {
		emit(data);
	}

	public DefaultRedstoneEmitter() {
		emit(0);
	}

	@Override
	public int getRedstoneSignal() {
		return data;
	}

	public void emit(int data) {
		if (data > 15) {
			this.data = 15;
		} else if (data < 0) {
			this.data = 0;
		} else {
			this.data = data;
		}
	}
}
