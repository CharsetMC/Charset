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

package pl.asie.charset.lib.wires;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataBands;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataDots;

public class SignalMeterDataBundledWire implements ISignalMeterDataBands, ISignalMeterDataDots {
	private byte[] data;

	public SignalMeterDataBundledWire() {

	}

	public SignalMeterDataBundledWire(byte[] data) {
		this.data = data != null ? data : new byte[16];
	}

	@Override
	public int getBandCount() {
		return 16;
	}

	@Override
	public int getBandColor(int i) {
		if (i == 15) {
			return 0x585858 | 0xFF000000;
		} else if (i == 7) {
			return 0x888888 | 0xFF000000;
		} else if (i == 8) {
			return 0xB0B0B0 | 0xFF000000;
		}
		return EnumDyeColor.byMetadata(i).getColorValue() | 0xFF000000;
	}

	@Override
	public float getBandHeight(int i) {
		return data[i] / 15f;
	}

	@Override
	public boolean areBandsHorizontal() {
		return false;
	}

	@Override
	public int getBandBackgroundColor(int i) {
		return (((getBandColor(i) >> 3) & 0x1F1F1F) | 0xFF000000) + 0x141414;
	}

	@Override
	public void serialize(PacketBuffer buffer) {
		buffer.writeBytes(data);
	}

	@Override
	public void deserialize(PacketBuffer buffer) {
		data = new byte[16];
		buffer.readBytes(data);
	}

	@Override
	public int getDotCount() {
		return 16;
	}

	@Override
	public int getDotColor(int i) {
		return (data[i] != 0) ? 0xFFCC1111 : 0x0;
	}
}
