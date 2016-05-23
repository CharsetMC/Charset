package pl.asie.charset.lib.capability;

import java.util.Collection;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.fmp.capabilities.ICapabilityWrapper;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.lib.Capabilities;

public class RedstoneEmitterWrapper implements ICapabilityWrapper<IRedstoneEmitter> {
	@Override
	public Capability<IRedstoneEmitter> getCapability() {
		return Capabilities.REDSTONE_EMITTER;
	}

	@Override
	public IRedstoneEmitter wrapImplementations(Collection<IRedstoneEmitter> collection) {
		int data = 0;

		for (IRedstoneEmitter emitter : collection) {
			data = Math.max(data, emitter.getRedstoneSignal());
		}

		return new DefaultRedstoneEmitter(data);
	}
}
