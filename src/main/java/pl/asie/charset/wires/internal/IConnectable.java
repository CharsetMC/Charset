package pl.asie.charset.wires.internal;

import net.minecraft.util.EnumFacing;

public interface IConnectable {
	boolean canConnect(IWire wire, WireLocation side, EnumFacing direction);
}
