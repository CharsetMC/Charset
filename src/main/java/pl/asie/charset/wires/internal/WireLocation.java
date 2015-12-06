package pl.asie.charset.wires.internal;

import net.minecraft.util.EnumFacing;

public enum WireLocation {
	DOWN,
	UP,
	NORTH,
	SOUTH,
	WEST,
	EAST,
	FREESTANDING;

	public static final WireLocation[] VALUES = values();

	public EnumFacing facing() {
		return ordinal() >= 6 ? null : EnumFacing.getFront(ordinal());
	}

	public static WireLocation get(EnumFacing facing) {
		return facing != null ? VALUES[facing.ordinal()] : FREESTANDING;
	}
}
