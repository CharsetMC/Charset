package pl.asie.charset.wires.internal;

import net.minecraft.util.EnumFacing;

/**
 * Marker interface. Do not implement!
 */
public interface IWire {
	boolean wireConnected(WireLocation side, EnumFacing direction);
	boolean wireConnectedCorner(WireLocation side, EnumFacing direction);
}
