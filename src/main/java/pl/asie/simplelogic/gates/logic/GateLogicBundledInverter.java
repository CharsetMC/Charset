/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.simplelogic.gates.logic;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public class GateLogicBundledInverter extends GateLogic {
	@Override
	public boolean tick(IGateContainer gate) {
		return true;
	}

	@Override
	public void calculateOutputBundled(EnumFacing side, byte[] data) {
		byte[] input = getInputValueBundled(EnumFacing.SOUTH);
		for (int i = 0; i < 16; i++) {
			data[i] = (byte) (input != null ? (15 - MathHelper.clamp(input[i], 0, 15)) : 15);
		}
	}

	@Override
	public GateRenderState getLayerState(int id) {
		return GateRenderState.DISABLED;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		return GateRenderState.OFF;
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		switch (dir) {
			case SOUTH:
				return GateConnection.INPUT_BUNDLED;
			case NORTH:
				return GateConnection.OUTPUT_BUNDLED;
			default:
				return GateConnection.NONE;
		}
	}
}
