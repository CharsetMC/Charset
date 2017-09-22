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

public class PartGateXOR extends PartGate {
	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.SOUTH) {
			return Connection.NONE;
		} else {
			return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
		}
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.NORTH));
			case 1:
				return State.input(getValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getValueInside(EnumFacing.EAST));
			case 3:
				return State.bool(getValueInside(EnumFacing.WEST) == 0 && getValueInside(EnumFacing.EAST) == 0);
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.WEST)).invert();
			case 1:
				return State.input(getValueInside(EnumFacing.EAST)).invert();
			case 2:
				return State.bool(getValueInside(EnumFacing.WEST) == 0 && getValueInside(EnumFacing.EAST) == 0).invert();
		}
		return State.ON;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return digiToRs(rsToDigi(getValueInside(EnumFacing.WEST)) ^ rsToDigi(getValueInside(EnumFacing.EAST)));
		} else {
			return 0;
		}
	}
}
