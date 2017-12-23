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

import com.google.common.collect.*;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TLongHashSet;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.patchwork.CharsetPatchwork;

import javax.annotation.Nullable;
import java.util.*;

public class LaserWorldStorage implements IWorldEventListener {
	protected final Long2ObjectOpenHashMap<Set<LaserBeam>> laserBeams = new Long2ObjectOpenHashMap<>();
	protected final Collection<LaserBeam> laserBeamView = new Collection<LaserBeam>() {
		@Override
		public int size() {
			int size = 0;
			for (Set<LaserBeam> set : laserBeams.values()) {
				size += set.size();
			}
			return size;
		}

		@Override
		public boolean isEmpty() {
			for (Set<LaserBeam> set : laserBeams.values()) {
				if (set.size() > 0) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean contains(Object o) {
			if (o instanceof LaserBeam) {
				Set<LaserBeam> beamSet = laserBeams.get(ChunkPos.asLong(((LaserBeam) o).getStart().getX() >> 4, ((LaserBeam) o).getStart().getZ() >> 4));
				return beamSet.contains(o);
			}
			return false;
		}

		@Override
		public Iterator<LaserBeam> iterator() {
			return new Iterator<LaserBeam>() {
				private final Iterator<Set<LaserBeam>> setIterator = laserBeams.values().iterator();
				private Iterator<LaserBeam> iterator;

				@Override
				public boolean hasNext() {
					return (iterator != null && iterator.hasNext()) || setIterator.hasNext();
				}

				@Override
				public LaserBeam next() {
					while (iterator == null || !iterator.hasNext()) {
						if (!setIterator.hasNext()) {
							return null;
						}
						iterator = setIterator.next().iterator();
					}
					return iterator.next();
				}
			};
		}

		@Override
		public Object[] toArray() {
			Object[] o = new Object[size()];
			int i = 0;
			for (Set<LaserBeam> set : laserBeams.values()) {
				for (LaserBeam beam : set) {
					o[i++] = beam;
				}
			}
			return o;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] ts) {
			int i = 0;
			for (Set<LaserBeam> set : laserBeams.values()) {
				for (LaserBeam beam : set) {
					if (i >= ts.length) return ts;
					ts[i++] = (T) beam;
				}
			}
			return ts;
		}

		@Override
		public boolean add(LaserBeam laserBeam) {
			long p = ChunkPos.asLong(laserBeam.getStart().getX() >> 4, laserBeam.getStart().getZ() >> 4);
			Set<LaserBeam> beamSet = laserBeams.get(p);
			if (beamSet == null) {
				beamSet = new LinkedHashSet<>();
				laserBeams.put(p, beamSet);
			}
			return beamSet.add(laserBeam);
		}

		@Override
		public boolean remove(Object o) {
			if (o instanceof LaserBeam) {
				long p = ChunkPos.asLong(((LaserBeam) o).getStart().getX() >> 4, ((LaserBeam) o).getStart().getZ() >> 4);
				Set<LaserBeam> beamSet = laserBeams.get(p);
				if (beamSet != null) {
					boolean r = beamSet.remove(o);
					if (r && beamSet.isEmpty()) {
						laserBeams.remove(p);
					}
					return r;
				}
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> collection) {
			for (Object o : collection) {
				if (!contains(o)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends LaserBeam> collection) {
			boolean a = false;
			for (Object o : collection) {
				a |= !add((LaserBeam) o);
			}
			return !a;
		}

		@Override
		public boolean removeAll(Collection<?> collection) {
			boolean a = false;
			for (Object o : collection) {
				a |= !remove(o);
			}
			return !a;
		}

		@Override
		public boolean retainAll(Collection<?> collection) {
			throw new RuntimeException("Implement me!");
		}

		@Override
		public void clear() {
			laserBeams.clear();
		}
	};

	private final Long2ObjectOpenHashMap<Set<ILaserEndpoint>> endpoints = new Long2ObjectOpenHashMap<>();
	private final LongSet validatedLasers = new LongOpenHashSet();
	private final LongSet chunksToRescan = new LongOpenHashSet();
	private final Queue<BlockPos> newLasersQueue = new ArrayDeque<>();
	protected final World world;
	private final boolean updates;

	private boolean isSearching = false;
	private Queue<Pair<TileEntity, EnumFacing>> positionsToCheck = new ArrayDeque<>();

	public LaserWorldStorage(World world, boolean updates) {
		this.world = world;
		this.updates = updates;
	}

	private void addChunkToRescan(BlockPos blockPos) {
		addChunkToRescan(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	private void addChunkToRescan(ChunkPos chunkPos) {
		addChunkToRescan(chunkPos.x, chunkPos.z);
	}

	private void addChunkToRescan(int x, int z) {
		chunksToRescan.add(ChunkPos.asLong(x, z));
	}

	protected boolean isEndpointHit(BlockPos pos, EnumFacing facing) {
		if (!CharsetPatchwork.LASER_REDSTONE) {
			throw new RuntimeException("Endpoint functionality not enabled! Please report to mod author.");
		}

		Set<ILaserEndpoint> set = endpoints.get(pos.toLong());
		if (set != null) {
			EnumFacing direction = facing;
			for (ILaserEndpoint endpoint : set) {
				if (endpoint.getDirection() == direction)
					return true;
			}
		}

		return false;
	}

	protected void addEndpoint(ILaserEndpoint endpoint) {
		if (CharsetPatchwork.LASER_REDSTONE) {
			long k = endpoint.getPos().toLong();
			Set<ILaserEndpoint> set = endpoints.get(k);
			if (set == null) {
				set = new HashSet<>();
				endpoints.put(k, set);
			}
			set.add(endpoint);
		}
	}

	protected void removeEndpoint(ILaserEndpoint endpoint) {
		if (CharsetPatchwork.LASER_REDSTONE) {
			long k = endpoint.getPos().toLong();
			Set<ILaserEndpoint> set = endpoints.get(k);
			if (set != null && set.remove(endpoint)) {
				if (set.isEmpty()) {
					endpoints.remove(k);
				}
			}
		}
	}

	public Collection<LaserBeam> getLaserBeams() {
		ImmutableSet.Builder<LaserBeam> beams = new ImmutableSet.Builder<>();
		for (Set<LaserBeam> set : laserBeams.values()) {
			beams.addAll(set);
		}
		return beams.build();
	}

	public void onTick() {
		if (updates)
			runChunkRescanQueue();
		else {
			chunksToRescan.clear();
			newLasersQueue.clear();
		}
	}

	private void respawnBeam(TileEntity tile, EnumFacing facing) {
		if (!updates) return;

		if (tile != null && tile.hasCapability(CharsetLaser.LASER_SOURCE, facing)) {
			positionsToCheck.add(Pair.of(tile, facing));

			if (!isSearching) checkPositions();
		}
	}

	private void checkPositions() {
		isSearching = true;

		while (!positionsToCheck.isEmpty()) {
			Pair<TileEntity, EnumFacing> pair = positionsToCheck.remove();
			TileEntity tile = pair.getLeft();
			EnumFacing facing = pair.getRight();

			LaserSource src = tile.getCapability(CharsetLaser.LASER_SOURCE, facing);
			if (src != null) {
				LaserBeam oldBeam = src.getBeam();
				src.updateBeam();
				LaserBeam newBeam = src.getBeam();

				if (oldBeam != newBeam) {
					if (oldBeam != null) {
						oldBeam.invalidate();
						remove(oldBeam, false);
					}

					if (newBeam != null) {
						add(newBeam);
					}
				}
			}
		}

		isSearching = false;
	}

	private void respawnAllBeams(TileEntity tile) {
		if (tile != null) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				respawnBeam(tile, facing);
			}
		}
	}

	// Avoid recreating the arrays every tick
	Queue<LaserBeam> lasersToRespawn = new ArrayDeque<>();

	private void runChunkRescanQueue() {
		while (!chunksToRescan.isEmpty()) {
			LongIterator it = chunksToRescan.iterator();
			while (it.hasNext()) {
				long cv = it.next();
				Set<LaserBeam> set = laserBeams.get(cv);
				if (set != null) {
					for (LaserBeam beam : set) {
						if (!validatedLasers.contains(beam.getId())) {
							if (!beam.isValid()) {
								lasersToRespawn.add(beam);
							} else {
								validatedLasers.add(beam.getId());
							}
						}
					}
				}
			}

			chunksToRescan.clear();

			for (LaserBeam beam : lasersToRespawn) {
				remove(beam, false);
			}

			while (!lasersToRespawn.isEmpty()) {
				LaserBeam beam = lasersToRespawn.remove();
				respawnBeam(beam.getWorld().getTileEntity(beam.getStart()), beam.getDirection());
			}
		}

		validatedLasers.clear();

		while (!newLasersQueue.isEmpty()) {
			BlockPos pos = newLasersQueue.remove();
			respawnAllBeams(world.getTileEntity(pos));
		}
	}

	public void removeAll(Chunk chunk) {
		long l = ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z);

		Collection<LaserBeam> removedBeams = laserBeams.get(l);
		if (removedBeams != null) {
			laserBeams.remove(l);

			for (LaserBeam laserBeam : removedBeams) {
				remove(laserBeam, true);
			}
		}
	}
/*
	public void rescanAll(Chunk chunk) {
		removeAll(chunk);

		for (TileEntity tile : chunk.getTileEntityMap().values()) {
			respawnAllBeams(tile);
		}
	}
*/

	public boolean add(LaserBeam beam) {
		if (laserBeamView.add(beam)) {
			if (/* updates && */(CharsetPatchwork.LASER_REDSTONE)) {
				addEndpoint(beam);
			}
			beam.onAdd(/* updates */ true);
			return true;
		} else {
			return false;
		}
	}

	public boolean remove(LaserBeam beam, boolean alreadyRemoved) {
		if (alreadyRemoved || laserBeamView.remove(beam)) {
			if (updates) {
				if (CharsetPatchwork.LASER_REDSTONE) {
					removeEndpoint(beam);
				}
				int cx1 = beam.getStart().getX() >> 4;
				int cz1 = beam.getStart().getZ() >> 4;
				int cx2 = beam.getEnd().getX() >> 4;
				int cz2 = beam.getEnd().getZ() >> 4;
				if (cx2 < cx1) {
					int t = cx1;
					cx1 = cx2;
					cx2 = t;
				}

				if (cz2 < cz1) {
					int t = cz1;
					cz1 = cz2;
					cz2 = t;
				}

				for (int cz = cz1; cz <= cz2; cz++)
					for (int cx = cx1; cx <= cx2; cx++)
						addChunkToRescan(cx, cz);
			}

			beam.onRemove(/* updates */ true);
			return true;
		} else {
			return false;
		}
	}

	protected int getChunkRadius() {
		// 1 = +0 chunks, 2 = +1 chunk ... 17 = +1 chunk, 18 = +2 chunks
		return (LaserBeam.MAX_DISTANCE + 14) / 16;
	}

	public void rescanAllAffectedChunks(ChunkPos c) {
		if (updates) {
			addChunkToRescan(c);
			int chunkDiff = getChunkRadius();
			int chunkX = c.x;
			int chunkZ = c.z;
			for (int d = -chunkDiff; d <= chunkDiff; d++) {
				addChunkToRescan(chunkX + d, chunkZ);
				addChunkToRescan(chunkX, chunkZ + d);
			}
		}
	}

	public void rescan(World worldIn, BlockPos pos) {
		// TODO: This is horrendous.
		rescanAllAffectedChunks(new ChunkPos(pos));
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		//if (oldState != newState) {
		validatedLasers.clear();
		rescan(worldIn, pos);
		//}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void onEntityAdded(Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(Entity entityIn) {

	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {

	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	public void registerLaserSources(BlockPos pos) {
		newLasersQueue.add(pos);
	}

	public void markLaserForUpdate(TileEntity tile, EnumFacing facing) {
		respawnBeam(tile, facing);
	}
}
