package pl.asie.charset.lib.capability;

import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneReceiver;

public class DummyRedstoneReceiver implements IBundledReceiver, IRedstoneReceiver {
	@Override
	public void onBundledInputChange() {
		
	}

	@Override
	public void onRedstoneInputChange() {

	}
}
