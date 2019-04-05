/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.audio.storage;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.network.PacketTile;

public class PacketUpdateProgressClient extends PacketTile {
	private float pc;

	public PacketUpdateProgressClient() {
		super();
	}

	public PacketUpdateProgressClient(TileRecordPlayer tile) {
		super(tile);
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeFloat(((TileRecordPlayer) tile).progressClient);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		pc = buf.readFloat();
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);
		if (tile != null && tile instanceof TileRecordPlayer) {
			((TileRecordPlayer) tile).progressClient = pc;
		}
	}

	@Override
	public boolean isAsynchronous() {
		return true;
	}
}
