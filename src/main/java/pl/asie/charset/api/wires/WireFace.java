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

	WireFace() {
		facing = ordinal() >= 6 ? null : EnumFacing.getFront(ordinal());
	}

	public static final WireFace[] VALUES = values();
	public final EnumFacing facing;

	public static WireFace get(EnumFacing facing) {
		return facing != null ? VALUES[facing.ordinal()] : CENTER;
	}
}
