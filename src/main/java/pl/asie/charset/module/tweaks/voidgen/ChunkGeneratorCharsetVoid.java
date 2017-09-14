package pl.asie.charset.module.tweaks.voidgen;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ChunkGeneratorCharsetVoid implements IChunkGenerator {
	private final World world;

	public ChunkGeneratorCharsetVoid(World worldIn) {
		this.world = worldIn;
		world.setSeaLevel(0);
	}

	@Override
	public Chunk generateChunk(int x, int z) {
		ChunkPrimer primer = new ChunkPrimer();
		Chunk chunk = new Chunk(world, primer, x, z);

		byte[] biomeArray = chunk.getBiomeArray();
		byte biomeId = (byte) Biome.getIdForBiome(Biomes.FOREST);
		for (int i = 0; i < biomeArray.length; i++) {
			biomeArray[i] = biomeId;
		}

		return chunk;
	}

	@Override
	public void populate(int x, int z) {

	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z) {

	}

	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
		return false;
	}
}
