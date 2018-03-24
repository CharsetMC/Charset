package pl.asie.charset.module.power.steam;

import net.minecraft.world.chunk.Chunk;

public class SteamChunkContainer {
	private final Chunk c;

	public SteamChunkContainer(Chunk c) {
		this.c = c;
	}

	@SuppressWarnings("ConstantConditions")
	public SteamChunkContainer() {
		this(new Chunk(null, 0, 0));
	}
}
