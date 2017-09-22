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

package pl.asie.charset.gates;

import net.minecraft.util.EnumFacing;

public class PartGateBuffer extends PartGate {
	public PartGateBuffer() {
		super();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side == EnumFacing.WEST || side == EnumFacing.EAST;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.SOUTH ? Connection.INPUT_ANALOG : Connection.OUTPUT;
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				if (!isSideOpen(EnumFacing.WEST)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.WEST));
			case 1:
				if (!isSideOpen(EnumFacing.EAST)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.EAST));
			case 2:
				return State.input(getValueInside(EnumFacing.NORTH)).invert();
			case 3:
				return State.input(getValueInside(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.NORTH));
			case 1:
				return State.input(getValueInside(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		switch (side) {
			case NORTH:
				return getValueInside(EnumFacing.SOUTH) > 0 ? 0 : (byte) 15;
			case WEST:
			case EAST:
				if (isSideInverted(EnumFacing.NORTH)) {
					return getValueInside(EnumFacing.SOUTH);
				} else {
					return getValueInside(EnumFacing.SOUTH) > 0 ? 0 : (byte) 15;
				}
			default:
				return 0;
		}
	}
}
