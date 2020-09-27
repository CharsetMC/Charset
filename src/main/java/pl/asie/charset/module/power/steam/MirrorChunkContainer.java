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

package pl.asie.charset.module.power.steam;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import pl.asie.charset.module.power.steam.api.IMirror;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

public class MirrorChunkContainer {
	private final Chunk c;
	private final TIntObjectMap<Collection<IMirror>> mirrorsByHeight = new TIntObjectHashMap<>();
	private final TIntObjectMap<IMirror> highestMirror = new TIntObjectHashMap<>();

	public MirrorChunkContainer(Chunk c) {
		this.c = c;
	}

	@SuppressWarnings("ConstantConditions")
	public MirrorChunkContainer() {
		this(new Chunk(null, 0, 0));
	}

	private int getHmPos(BlockPos pos) {
		return ((pos.getX() & 15) << 4) | (pos.getZ() & 15);
	}

	public Collection<IMirror> getMirrors(BlockPos pos) {
		Collection<IMirror> collection = mirrorsByHeight.get(pos.getY());
		return collection != null ? collection : Collections.emptySet();
	}

	public IMirror getHighestMirror(BlockPos pos) {
		return highestMirror.get(getHmPos(pos));
	}

	private void updateHmPos(IMirror mirror) {
		if (mirror.isMirrorValid()) {
			int hmPos = getHmPos(mirror.getMirrorPos());
			IMirror other = highestMirror.get(hmPos);
			if (other == null || other.getMirrorPos().getY() < mirror.getMirrorPos().getY()) {
				highestMirror.put(hmPos, mirror);
			}
		}
	}

	public void registerMirror(IMirror mirror) {
		Collection<IMirror> collection = mirrorsByHeight.get(mirror.getMirrorPos().getY());
		if (collection == null) {
			collection = new HashSet<>();
			mirrorsByHeight.put(mirror.getMirrorPos().getY(), collection);
		}
		collection.add(mirror);
		updateHmPos(mirror);
	}

	public void unregisterMirror(IMirror mirror) {
		Collection<IMirror> collection = mirrorsByHeight.get(mirror.getMirrorPos().getY());
		if (collection != null) {
			collection.remove(mirror);
		}

		int hmPos = getHmPos(mirror.getMirrorPos());
		IMirror other = highestMirror.get(hmPos);
		if (other == mirror || (other != null && !other.isMirrorValid())) {
			highestMirror.remove(hmPos);

			// TODO: make this faster
			for (TileEntity tile : c.getTileEntityMap().values()) {
				if (tile instanceof IMirror) {
					int hmPos2 = getHmPos(((IMirror) tile).getMirrorPos());
					if (hmPos2 == hmPos) {
						updateHmPos((IMirror) tile);
					}
				}
			}

			other = highestMirror.get(hmPos);
			if (other != null) {
				other.requestMirrorTargetRefresh();
			}
		}
	}

	public static IMirror getHighestMirror(World world, BlockPos pos) {
		Chunk c = world.getChunk(pos);
		if (c != null && c.hasCapability(CharsetPowerSteam.mirrorContainerCap, null)) {
			MirrorChunkContainer box = c.getCapability(CharsetPowerSteam.mirrorContainerCap, null);
			assert box != null;
			return box.getHighestMirror(pos);
		}

		return null;
	}

	public static void registerMirror(World world, IMirror mirror) {
		Chunk c = world.getChunk(mirror.getMirrorPos());
		if (c != null && c.hasCapability(CharsetPowerSteam.mirrorContainerCap, null)) {
			MirrorChunkContainer box = c.getCapability(CharsetPowerSteam.mirrorContainerCap, null);
			assert box != null;
			box.registerMirror(mirror);
		}
	}

	public static void unregisterMirror(World world, IMirror mirror) {
		Chunk c = world.getChunk(mirror.getMirrorPos());
		if (c != null && c.hasCapability(CharsetPowerSteam.mirrorContainerCap, null)) {
			MirrorChunkContainer box = c.getCapability(CharsetPowerSteam.mirrorContainerCap, null);
			assert box != null;
			box.unregisterMirror(mirror);
		}
	}

	public static void forEach(World world, BlockPos pos, Consumer<IMirror> consumer) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		for (int ix = chunkX - 1; ix <= chunkX + 1; ix++) {
			for (int iz = chunkZ - 1; iz <= chunkZ + 1; iz++) {
				Chunk c = world.getChunk(ix, iz);
				if (c != null && c.hasCapability(CharsetPowerSteam.mirrorContainerCap, null)) {
					MirrorChunkContainer box = c.getCapability(CharsetPowerSteam.mirrorContainerCap, null);
					for (Collection<IMirror> collection : box.mirrorsByHeight.valueCollection()) {
						collection.forEach(consumer);
					}
				}
			}
		}
	}

	public static void forEachListening(World world, BlockPos pos, Consumer<IMirror> consumer) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		for (int ix = chunkX - 1; ix <= chunkX + 1; ix++) {
			for (int iz = chunkZ - 1; iz <= chunkZ + 1; iz++) {
				Chunk c = world.getChunk(ix, iz);
				if (c != null && c.hasCapability(CharsetPowerSteam.mirrorContainerCap, null)) {
					MirrorChunkContainer box = c.getCapability(CharsetPowerSteam.mirrorContainerCap, null);
					box.getMirrors(pos).forEach(consumer);
				}
			}
		}
	}
}
