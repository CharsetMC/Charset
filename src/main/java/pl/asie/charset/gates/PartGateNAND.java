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

public class PartGateNAND extends PartGate {
	public PartGateNAND() {
		super();
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.NORTH));
			case 1:
				if (!isSideOpen(EnumFacing.WEST)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.WEST));
			case 2:
				if (!isSideOpen(EnumFacing.EAST)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.EAST));
			case 3:
				if (!isSideOpen(EnumFacing.SOUTH)) {
					return State.DISABLED;
				}
				return State.input(getValueInside(EnumFacing.SOUTH));
			case 4:
				return State.input(getValueOutside(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.WEST)).invert();
			case 1:
				return State.input(getValueInside(EnumFacing.SOUTH)).invert();
			case 2:
				return State.input(getValueInside(EnumFacing.EAST)).invert();
			case 3:
				return State.input(getValueInside(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (isSideOpen(facing) && facing != EnumFacing.NORTH) {
					if (getValueInside(facing) == 0) {
						return 15;
					}
				}
			}
		}
		return 0;
	}
}
