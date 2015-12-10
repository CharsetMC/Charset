package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

public enum WireFace {
	DOWN,
	UP,
	NORTH,
	SOUTH,
	WEST,
	EAST,
	CENTER;

	public static final WireFace[] VALUES = values();

	public EnumFacing facing() {
		return ordinal() >= 6 ? null : EnumFacing.getFront(ordinal());
	}

	public static WireFace get(EnumFacing facing) {
		return facing != null ? VALUES[facing.ordinal()] : CENTER;
	}
}
