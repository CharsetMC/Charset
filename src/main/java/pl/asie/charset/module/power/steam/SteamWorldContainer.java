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
