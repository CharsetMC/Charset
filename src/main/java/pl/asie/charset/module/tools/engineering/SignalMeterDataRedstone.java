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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataBands;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataDots;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataRemoteProvider;

import java.io.IOException;

public class SignalMeterDataRedstone implements ISignalMeterDataBands {
	@Override
	public int getBandCount() {
		return 1;
	}

	@Override
	public int getBandColor(int i) {
		int signalValue = v;
		int colCpt = (signalValue > 0 ? 0x96 : 0x78) + (signalValue * 7);
		return 0xFF000000 | (colCpt << 16);
	}

	@Override
	public float getBandHeight(int i) {
		return v / 15f;
	}

	@Override
	public boolean areBandsHorizontal() {
		return false;
	}

	public static class Provider implements ISignalMeterDataRemoteProvider {
		@Override
		public ISignalMeterData getSignalMeterData(IBlockAccess world, BlockPos pos, RayTraceResult result) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockRedstoneWire) {
				return new SignalMeterDataRedstone(state.getValue(BlockRedstoneWire.POWER));
			} else {
				return null;
			}
		}
	}

	private byte v;

	public SignalMeterDataRedstone() {

	}

	public SignalMeterDataRedstone(int value) {
		v = (byte) value;
	}

	@Override
	public void serialize(PacketBuffer buffer) {
		buffer.writeByte(v);
	}

	@Override
	public void deserialize(PacketBuffer buffer) {
		v = buffer.readByte();
	}
}
