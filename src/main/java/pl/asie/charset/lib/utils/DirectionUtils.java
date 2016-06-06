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

package pl.asie.charset.lib.utils;

import net.minecraft.util.EnumFacing;

public final class DirectionUtils {
	private DirectionUtils() {

	}

	public static final EnumFacing[] NEGATIVES = {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.WEST};
	public static final EnumFacing[] POSITIVES = {EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.EAST};

	public static int ordinal(EnumFacing side) {
		return side == null ? 6 : side.ordinal();
	}

	public static EnumFacing get(int ordinal) {
		return ordinal == 6 ? null : EnumFacing.getFront(ordinal);
	}
}
