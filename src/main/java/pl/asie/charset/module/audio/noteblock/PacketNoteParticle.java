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

package pl.asie.charset.module.audio.noteblock;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.lib.network.PacketTile;

public class PacketNoteParticle extends PacketTile {
	protected int note;

	public PacketNoteParticle() {
		super();
	}

	public PacketNoteParticle(TileEntity tile, int note) {
		this.tile = tile;
		this.note = note;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		note = buf.readByte();
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);
		if (tile != null) {
			BlockPos pos = tile.getPos();
			tile.getWorld().spawnParticle(EnumParticleTypes.NOTE, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.2D, (double) pos.getZ() + 0.5D,
					(double) note / 24.0D, 0.0D, 0.0D, new int[0]);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeByte(note);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
