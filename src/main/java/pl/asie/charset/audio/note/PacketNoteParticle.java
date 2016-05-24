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

package pl.asie.charset.audio.note;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;

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
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		note = buf.readByte();
		if (tile != null) {
			BlockPos pos = tile.getPos();
			double noteX = (((note - TileIronNote.MIN_NOTE) % 6) + 0.5D) / 5.0D;
			double noteZ = (((note - TileIronNote.MIN_NOTE) / 6) + 0.5D) / 5.0D;
			tile.getWorld().spawnParticle(EnumParticleTypes.NOTE, (double) pos.getX() + noteX, (double) pos.getY() + 1.2D, (double) pos.getZ() + noteZ,
					(double) (note - TileIronNote.MIN_NOTE) / (double) (TileIronNote.MAX_NOTE - TileIronNote.MIN_NOTE), 0.0D, 0.0D, new int[0]);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeByte(note);
	}
}
