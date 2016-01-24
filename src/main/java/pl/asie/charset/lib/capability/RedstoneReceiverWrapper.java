package pl.asie.charset.lib.capability;

import java.util.Collection;

import net.minecraftforge.common.capabilities.Capability;

import mcmultipart.capabilities.ICapabilityWrapper;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.Capabilities;

public class RedstoneReceiverWrapper implements ICapabilityWrapper<IRedstoneReceiver> {
	private class WrappedReceiver implements IRedstoneReceiver {
		private final Collection<IRedstoneReceiver> receiverSet;

		public WrappedReceiver(Collection<IRedstoneReceiver> receiverSet) {
			this.receiverSet = receiverSet;
		}

		@Override
		public void onRedstoneInputChange() {
			for (IRedstoneReceiver r : receiverSet) {
				r.onRedstoneInputChange();
			}
		}
	}

	@Override
	public Capability<IRedstoneReceiver> getCapability() {
		return Capabilities.REDSTONE_RECEIVER;
	}

	@Override
	public IRedstoneReceiver wrapImplementations(Collection<IRedstoneReceiver> collection) {
		return new WrappedReceiver(collection);
	}
}
