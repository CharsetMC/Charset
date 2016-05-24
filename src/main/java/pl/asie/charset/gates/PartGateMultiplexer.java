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

public class PartGateMultiplexer extends PartGate {
	public PartGateMultiplexer() {
		super();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public State getLayerState(int id) {
		boolean isWest = getValueInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getValueInside(EnumFacing.WEST) != 0;
		boolean eastOn = getValueInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH));
			case 1:
				return State.input(getValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getValueInside(EnumFacing.EAST));
			case 3:
				return State.bool(isWest && !westOn);
			case 4:
				return State.bool(!isWest && !eastOn);
			case 5:
				return State.input(getValueInside(EnumFacing.SOUTH)).invert();
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		boolean isWest = getValueInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getValueInside(EnumFacing.WEST) != 0;
		boolean eastOn = getValueInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH)).invert();
			case 1:
				return (!isWest || westOn) ? State.OFF : State.ON;
			case 2:
				return (isWest || eastOn) ? State.OFF : State.ON;
			case 3:
				return State.input(getValueOutside(EnumFacing.SOUTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		boolean isWest = getValueInside(EnumFacing.SOUTH) != 0;
		return isWest ? getValueInside(EnumFacing.WEST) : getValueInside(EnumFacing.EAST);
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? Connection.OUTPUT_ANALOG :
				(dir == EnumFacing.SOUTH ? Connection.INPUT : Connection.INPUT_ANALOG);
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}
}
