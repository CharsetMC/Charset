package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

import mcmultipart.multipart.PartSlot;

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
        slot = PartSlot.values()[ordinal()];
    }

	public static final WireFace[] VALUES = values();
    public final EnumFacing facing;
    public final PartSlot slot;

	public static WireFace get(EnumFacing facing) {
		return facing != null ? VALUES[facing.ordinal()] : CENTER;
	}
    public static WireFace get(PartSlot slot) {
        return slot.ordinal() < 7 ? VALUES[slot.ordinal()] : null;
    }
}
