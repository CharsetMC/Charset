package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

public interface IBundledEmitter {
	/**
	 * Get the signal strength of a bundled signal emitter.
	 * @param face The face the signal is on. Use null to get the strongest signals emitted in a given direction.
	 * @param toDirection The direction signal is being emitted to.
	 * @return A byte array of bundled signal strengths, 0-15 each.
	 */
	byte[] getBundledSignal(WireFace face, EnumFacing toDirection);
}
