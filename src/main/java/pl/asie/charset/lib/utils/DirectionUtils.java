package pl.asie.charset.lib.utils;

import net.minecraft.util.EnumFacing;

public final class DirectionUtils {
	private DirectionUtils() {

	}

	public static int ordinal(EnumFacing side) {
		return side == null ? 6 : side.ordinal();
	}

	public static EnumFacing get(int ordinal) {
		return ordinal == 6 ? null : EnumFacing.getFront(ordinal);
	}
}
