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

package pl.asie.charset.module.storage.barrels;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.network.PacketTile;

public class PacketBarrelCountUpdate extends PacketTile {
	protected int count;

	public PacketBarrelCountUpdate() {
		super();
	}

	public PacketBarrelCountUpdate(TileEntityDayBarrel tile) {
		super(tile);
		count = tile.getItemCount();
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeInt(count);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		count = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);
		if (tile instanceof TileEntityDayBarrel) {
			((TileEntityDayBarrel) tile).onCountUpdate(this);
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
