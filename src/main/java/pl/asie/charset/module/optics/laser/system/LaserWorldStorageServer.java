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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkWatchEvent;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.module.optics.laser.CharsetLaser;

import java.util.Set;

public class LaserWorldStorageServer extends LaserWorldStorage {
	public static boolean IS_LAZY = false;
	public static int LAZY_LIGHT_DELAY = 1;

	public LaserWorldStorageServer(World world) {
		super(world, true);
	}

	@Override
	public boolean add(LaserBeam beam) {
		if (IS_LAZY) {
			Scheduler.INSTANCE.in(world, LAZY_LIGHT_DELAY, () -> {
				if (beam.isValid()) {
					realAdd(beam);
				} else {
					markLaserForUpdate(world.getTileEntity(beam.getStart()), beam.getDirection());
				}
			});
			return true;
		} else {
			return realAdd(beam);
		}
	}

	private boolean realAdd(LaserBeam beam) {
		if (super.add(beam)) {
			sendPacket(new PacketBeamAdd(beam), beam);
			return true;
		} else {
			return false;
		}
	}

	protected void onChunkWatchingStart(ChunkWatchEvent.Watch event) {
		EntityPlayer player = event.getPlayer();
		ChunkPos pos = event.getChunk();

		Set<LaserBeam> set = laserBeams.get(ChunkPos.asLong(pos.x, pos.z));
		if (set != null) {
			for (LaserBeam beam : set) {
				CharsetLaser.packet.sendTo(new PacketBeamAdd(beam), player);
			}
		}
	}

	private void sendPacket(Packet packet, LaserBeam beam) {
/*		List<ChunkPos> chunks = new ArrayList<>();

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
				chunks.add(new ChunkPos(cx,cz));

		WorldServer worldServer = (WorldServer) beam.getWorld();
		PlayerChunkMap map = worldServer.getPlayerChunkMap();

		for (EntityPlayer player : worldServer.playerEntities) {
			if (map.isPlayerWatchingChunk((EntityPlayerMP) player, cx, cz)) {
				CharsetLaser.packet.sendTo(packet, player);
				break;
			}
		}
		*/

		CharsetLaser.packet.sendToWatching(packet, beam.getWorld(), beam.getStart(), null);
	}

	@Override
	public boolean remove(LaserBeam beam, boolean alreadyRemoved) {
		if (IS_LAZY) {
			Scheduler.INSTANCE.in(world, LAZY_LIGHT_DELAY, () -> {
				realRemove(beam, alreadyRemoved);
			});
			return true;
		} else {
			return realRemove(beam, alreadyRemoved);
		}
	}

	private boolean realRemove(LaserBeam beam, boolean alreadyRemoved) {
		if (super.remove(beam, alreadyRemoved)) {
			sendPacket(new PacketBeamRemove(beam), beam);
			return true;
		} else {
			return false;
		}
	}
}
