/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	public int width() {
		return WireUtils.width(type);
	}

	public int height() {
		return WireUtils.height(type);
	}
}
