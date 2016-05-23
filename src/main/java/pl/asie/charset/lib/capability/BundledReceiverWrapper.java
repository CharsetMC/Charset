package pl.asie.charset.lib.capability;

import java.util.Collection;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.fmp.capabilities.ICapabilityWrapper;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.lib.Capabilities;

public class BundledReceiverWrapper implements ICapabilityWrapper<IBundledReceiver> {
	private class WrappedReceiver implements IBundledReceiver {
		private final Collection<IBundledReceiver> receiverSet;

		public WrappedReceiver(Collection<IBundledReceiver> receiverSet) {
			this.receiverSet = receiverSet;
		}

		@Override
		public void onBundledInputChange() {
			for (IBundledReceiver r : receiverSet) {
				r.onBundledInputChange();
			}
		}
	}

	@Override
	public Capability<IBundledReceiver> getCapability() {
		return Capabilities.BUNDLED_RECEIVER;
	}

	@Override
	public IBundledReceiver wrapImplementations(Collection<IBundledReceiver> collection) {
		return new WrappedReceiver(collection);
	}
}
