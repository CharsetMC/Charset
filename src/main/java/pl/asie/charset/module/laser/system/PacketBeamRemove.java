/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.laser.system;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.module.laser.CharsetLaser;

public class PacketBeamRemove extends Packet {
	private LaserBeam beam;
	private World world;
	private long id;

	public PacketBeamRemove(LaserBeam beam) {
		this.beam = beam;
	}

	public PacketBeamRemove() {

	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		try {
			id = buf.readLong();
			world = getPlayer(handler).getEntityWorld();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void apply(INetHandler handler) {
		CharsetLaser.laserStorage.remove(world, id);
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeLong(beam.getId());
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
