package pl.asie.charset.wires;

public enum WireType {
	NORMAL(0, 0),
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
	BUNDLED(2, 0);

	public enum Type {
		NORMAL,
		INSULATED,
		BUNDLED
	}

	public static final WireType[] VALUES = values();
	private static final WireType[] insulatedTypes = new WireType[16];
	private final Type type;
	private final int color;

	static {
		for (WireType type : values()) {
			if (type.type() == Type.INSULATED) {
				insulatedTypes[type.color()] = type;
			}
		}
	}

	WireType(int type, int color) {
		this.type = Type.values()[type];
		this.color = color;
	}

	public static WireType insulated(int color) {
		return insulatedTypes[color];
	}

	public Type type() {
		return type;
	}

	public int color() {
		return color;
	}

	public boolean connects(WireType type2) {
		switch (type) {
			case NORMAL:
				return type2.type != Type.BUNDLED;
			case INSULATED:
				return type2.type != Type.INSULATED || type2.color == color;
			case BUNDLED:
				return type2.type != Type.NORMAL;
		}

		return false;
	}
}
