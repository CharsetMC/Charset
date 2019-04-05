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

package pl.asie.charset.module.power.steam;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.network.Packet;

public class PacketSpawnParticle extends Packet {
	private SteamParticle particle;
	private int dimId;
	private double x, y, z;
	private float xM, yM, zM;
	private int lifetime, value;

	public PacketSpawnParticle() {

	}

	public PacketSpawnParticle(SteamParticle particle) {
		this.particle = particle;
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeInt(particle.world.provider.getDimension());
		buf.writeDouble(particle.x);
		buf.writeDouble(particle.y);
		buf.writeDouble(particle.z);
		buf.writeFloat((float) particle.xMotion);
		buf.writeFloat((float) particle.yMotion);
		buf.writeFloat((float) particle.zMotion);
		buf.writeInt(particle.lifetime);
		buf.writeInt(particle.value);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		dimId = buf.readInt();
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		xM = buf.readFloat();
		yM = buf.readFloat();
		zM = buf.readFloat();
		lifetime = buf.readInt();
		value = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		World w = getWorld(handler, dimId);
		if (w != null) {
			SteamWorldContainer ctr = w.getCapability(CharsetPowerSteam.steamWorldCap, null);
			assert ctr != null;
			ctr.spawnParticle(new SteamParticle(w, x, y, z, xM, yM, zM, lifetime, value));
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
