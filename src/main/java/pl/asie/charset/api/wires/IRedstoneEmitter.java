package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

public interface IRedstoneEmitter {
	/**
	 * Get the signal strength of a redstone signal emitter.
	 * @param face The face the signal is on. Use null to get the strongest signal emitted in a given direction.
	 * @return The signal strength, 0-15.
	 */
	int getRedstoneSignal(WireFace face, EnumFacing toDirection);
}
