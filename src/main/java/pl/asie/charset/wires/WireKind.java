package pl.asie.charset.wires;

import pl.asie.charset.api.wires.WireType;

public enum WireKind {
	NORMAL(0, -1),
	INSULATED_0(1, 0),
	INSULATED_1(1, 1),
	INSULATED_2(1, 2),
	INSULATED_3(1, 3),
	INSULATED_4(1, 4),
	INSULATED_5(1, 5),
	INSULATED_6(1, 6),
	INSULATED_7(1, 7),
	INSULATED_8(1, 8),
	INSULATED_9(1, 9),
	INSULATED_10(1, 10),
	INSULATED_11(1, 11),
	INSULATED_12(1, 12),
	INSULATED_13(1, 13),
	INSULATED_14(1, 14),
	INSULATED_15(1, 15),
	BUNDLED(2, -1);

	public static final WireKind[] VALUES = values();
	private static final WireKind[] insulatedTypes = new WireKind[16];
	private final WireType type;
	private final int color;

	static {
		for (WireKind type : values()) {
			if (type.type() == WireType.INSULATED) {
				insulatedTypes[type.color()] = type;
			}
		}
	}

	WireKind(int type, int color) {
		this.type = WireType.values()[type];
		this.color = color;
	}

	public static WireKind insulated(int color) {
		return insulatedTypes[color];
	}

	public WireType type() {
		return type;
	}

	public int color() {
		return color;
	}

	public boolean connects(WireKind type2) {
		switch (type) {
			case NORMAL:
				return type2.type != WireType.BUNDLED;
			case INSULATED:
				return type2.type != WireType.INSULATED || type2.color == color;
			case BUNDLED:
				return type2.type != WireType.NORMAL;
		}

		return false;
	}

	public int height() {
		switch (type) {
			case NORMAL:
				return 2;
			case INSULATED:
				return 3;
			case BUNDLED:
				return 4;
		}

		return 0;
	}
}
