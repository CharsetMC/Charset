package pl.asie.charset.wires.internal;

import net.minecraft.util.EnumFacing;

public interface IRedstoneEmitter {
	/**
	 * Get the signal strength of a redstone emitter.
	 * @param direction
	 * @return Strength (0-15)
	 */
	int getSignalStrength(EnumFacing direction);
}
