package pl.asie.charset.lib.capability.lib;

import mcmultipart.capabilities.ICapabilityWrapper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.lib.capability.Capabilities;

import java.util.Collection;
import java.util.List;

public class DebuggableWrapper implements ICapabilityWrapper<IDebuggable> {
	private class Wrapped implements IDebuggable {
		private final Collection<IDebuggable> receivers;

		Wrapped(Collection<IDebuggable> receivers) {
			this.receivers = receivers;
		}

		@Override
		public void addDebugInformation(List<String> stringList, Side side) {
			for (IDebuggable debug : receivers)
				debug.addDebugInformation(stringList, side);
		}
	}

	@Override
	public Capability<IDebuggable> getCapability() {
		return Capabilities.DEBUGGABLE;
	}

	@Override
	public IDebuggable wrapImplementations(Collection<IDebuggable> implementations) {
		return new Wrapped(implementations);
	}
}