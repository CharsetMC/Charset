/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.lib.wires;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataBands;

public class SignalMeterDataWire implements ISignalMeterDataBands {
	private byte v;
	private int color;

	public SignalMeterDataWire() {

	}

	public SignalMeterDataWire(byte v, int color) {
		this.v = v;
		this.color = color;
	}

	@Override
	public int getBandCount() {
		return 1;
	}

	@Override
	public int getBandColor(int i) {
		if (color >= 0) {
			return EnumDyeColor.byMetadata(color).getColorValue() | 0xFF000000;
		} else {
			int signalValue = v;
			int colCpt = (signalValue > 0 ? 0x96 : 0x78) + (signalValue * 7);
			return 0xFF000000 | (colCpt << 16);
		}
	}

	@Override
	public float getBandHeight(int i) {
		return v / 15f;
	}

	@Override
	public boolean areBandsHorizontal() {
		return false;
	}

	@Override
	public void serialize(PacketBuffer buffer) {
		buffer.writeByte(v);
		buffer.writeByte(color);
	}

	@Override
	public void deserialize(PacketBuffer buffer) {
		v = buffer.readByte();
		color = buffer.readByte();
	}
}
