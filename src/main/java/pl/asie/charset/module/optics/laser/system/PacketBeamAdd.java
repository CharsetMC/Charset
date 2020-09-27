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

package pl.asie.charset.module.optics.laser.system;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.module.optics.laser.CharsetLaser;

public class PacketBeamAdd extends Packet {
	private LaserBeam beam;
	private long id;
	private int dim;
	private BlockPos start;
	private int length, flags;

	public PacketBeamAdd(LaserBeam beam) {
		this.beam = beam;
	}

	public PacketBeamAdd() {

	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		id = buf.readLong();
		dim = buf.readInt();
		start = buf.readBlockPos();
		length = buf.readUnsignedShort();
		flags = buf.readUnsignedByte();
	}

	@Override
	public void apply(INetHandler handler) {
		World world = getWorld(handler, dim);
		if (world != null) {
			CharsetLaser.laserStorage.add(beam = new LaserBeam(id, world, start, length, flags));
		} else {
			ModCharset.logger.warn("Could not find dimension " + dim + " for laser beam!");
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		beam.writeData(buf);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
