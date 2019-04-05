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

import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SteamChunkContainer implements ITickable {
	private final Chunk c;
	private final List<SteamParticle> particleList = new LinkedList<>();

	public SteamChunkContainer(Chunk c) {
		this.c = c;
	}

	@SuppressWarnings("ConstantConditions")
	public SteamChunkContainer() {
		this(new Chunk(null, 0, 0));
	}

	public void spawnParticle(SteamParticle particle) {
		particleList.add(particle);
		if (!c.getWorld().isRemote) {
			CharsetPowerSteam.packet.sendToWatching(new PacketSpawnParticle(particle), particle.world, new BlockPos((int) particle.x, (int) particle.y, (int) particle.z), null);
		}
	}

	@Override
	public void update() {
		Iterator<SteamParticle> iterator = particleList.iterator();
		while (iterator.hasNext()) {
			SteamParticle particle = iterator.next();
			particle.update();
			if (particle.isInvalid()) {
				iterator.remove();
			}
		}
	}

	public Collection<SteamParticle> getParticles() {
		return particleList;
	}

	public Chunk getChunk() {
		return c;
	}
}
