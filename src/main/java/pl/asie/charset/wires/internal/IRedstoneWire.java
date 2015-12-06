package pl.asie.charset.wires.internal;

import net.minecraft.util.EnumFacing;

/**
 * Marker interface. Do not implement!
 */
public interface IRedstoneWire extends IRedstoneEmitter, IRedstoneUpdatable, IWire {
	int getSignalStrength(WireLocation side, EnumFacing direction);
}
