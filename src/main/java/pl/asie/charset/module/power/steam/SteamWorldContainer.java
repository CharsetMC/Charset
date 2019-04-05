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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Collection;

public class SteamWorldContainer {
	private TLongObjectMap<SteamChunkContainer> containers = new TLongObjectHashMap<>();

	public void onChunkLoaded(Chunk c) {
		containers.put(ChunkPos.asLong(c.x, c.z), c.getCapability(CharsetPowerSteam.steamContainerCap, null));
	}

	public void onChunkUnloaded(Chunk c) {
		containers.remove(ChunkPos.asLong(c.x, c.z));
	}

	public SteamChunkContainer getContainer(BlockPos pos) {
		return containers.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
	}

	public Collection<SteamChunkContainer> getAllContainers() {
		return containers.valueCollection();
	}

	public void spawnParticle(SteamParticle particle) {
		SteamChunkContainer cc = getContainer(new BlockPos(particle.x, particle.y, particle.z));
		if (cc != null) {
			cc.spawnParticle(particle);
		}
	}
}
