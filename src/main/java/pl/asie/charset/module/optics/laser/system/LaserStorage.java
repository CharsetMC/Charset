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

package pl.asie.charset.module.optics.laser.system;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class LaserStorage {
	private final Map<World, LaserWorldStorage> storageMap = new IdentityHashMap<>();

	public Collection<LaserBeam> getLaserBeams(World world) {
		if (storageMap.containsKey(world)) {
			return storageMap.get(world).getLaserBeams();
		} else {
			return Collections.emptyList();
		}
	}

	// API calls

	public void markLaserForUpdate(TileEntity tile, EnumFacing facing) {
		if (storageMap.containsKey(tile.getWorld())) {
			storageMap.get(tile.getWorld()).markLaserForUpdate(tile, facing);
		}
	}

	public void registerLaserSources(World world, BlockPos pos) {
		if (storageMap.containsKey(world)) {
			storageMap.get(world).registerLaserSources(pos);
		}
	}

	// Updates

	private LaserWorldStorage getOrCreateStorage(World world) {
		if (storageMap.containsKey(world)) {
			return storageMap.get(world);
		} else {
			LaserWorldStorage storage = world.isRemote ? new LaserWorldStorageClient(world) : new LaserWorldStorageServer(world);
			storageMap.put(world, storage);
			return storage;
		}
	}

	@SubscribeEvent
	public void onStartWatching(ChunkWatchEvent.Watch event) {
		EntityPlayer player = event.getPlayer();
		if (storageMap.containsKey(event.getPlayer().getServerWorld())) {
			((LaserWorldStorageServer) storageMap.get(event.getPlayer().getServerWorld())).onChunkWatchingStart(event);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && storageMap.containsKey(event.world)) {
			storageMap.get(event.world).onTick();
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		event.getWorld().addEventListener(getOrCreateStorage(event.getWorld()));
		// storage.rescanAll(event.getWorld());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		storageMap.remove(event.getWorld());
	}

	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event) {
		// TODO: More efficient implementation?
		if (storageMap.containsKey(event.getWorld())) {
			storageMap.get(event.getWorld()).rescan(event.getWorld(), event.getPos());
		}
	}

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		// TODO: More efficient implementation?
		if (storageMap.containsKey(event.getWorld())) {
			storageMap.get(event.getWorld()).rescan(event.getWorld(), event.getPos());
		}
	}

	/* @SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		// TODO: More efficient implementation?
		if (storageMap.containsKey(event.getWorld())) {
			storageMap.get(event.getWorld()).rescanAll(event.getChunk());
		}
	} */

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event) {
		// TODO: More efficient implementation?
		storageMap.get(event.getWorld()).removeAll(event.getChunk());
		storageMap.get(event.getWorld()).rescanAllAffectedChunks(event.getChunk().getPos());
	}

	// Redstone helper

	protected boolean isEndpointHit(World world, BlockPos pos, EnumFacing facing) {
		if (storageMap.containsKey(world)) {
			return storageMap.get(world).isEndpointHit(pos, facing);
		} else {
			return false;
		}
	}

	// Packet use

	protected void add(LaserBeam beam) {
		getOrCreateStorage(beam.getWorld()).add(beam);
	}

	protected void remove(World world, long id) {
		LaserWorldStorage storage = getOrCreateStorage(world);
		if (storage instanceof LaserWorldStorageClient) {
			((LaserWorldStorageClient) storage).removeById(id);
		}
	}

	protected void remove(LaserBeam beam) {
		getOrCreateStorage(beam.getWorld()).remove(beam, false);
	}
}
