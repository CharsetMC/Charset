package pl.asie.charset.lib.capability;

import java.util.Collection;

import net.minecraftforge.common.capabilities.Capability;

import mcmultipart.capabilities.ICapabilityWrapper;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.lib.Capabilities;

public class BundledEmitterWrapper implements ICapabilityWrapper<IBundledEmitter> {
	@Override
	public Capability<IBundledEmitter> getCapability() {
		return Capabilities.BUNDLED_EMITTER;
	}

	@Override
	public IBundledEmitter wrapImplementations(Collection<IBundledEmitter> collection) {
		byte[] data = new byte[16];

		for (IBundledEmitter emitter : collection) {
			byte[] dataIn = emitter.getBundledSignal();
			if (dataIn != null) {
				for (int i = 0; i < 16; i++) {
					data[i] = (byte) Math.max(0xFF & (int) dataIn[i], 0xFF & (int) data[i]);
				}
			}
		}

		return new DefaultBundledEmitter(data);
	}
}
