package pl.asie.charset.lib.capability.lib;

import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class DebuggableWrapper implements Function<List<IDebuggable>, IDebuggable> {
	@Override
	public IDebuggable apply(List<IDebuggable> iDebuggables) {
		return new Wrapped(iDebuggables);
	}

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
}