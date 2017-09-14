package pl.asie.charset.module.tweaks.voidgen;

import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderCharsetVoid extends WorldProviderSurface {
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkGeneratorCharsetVoid(world);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getVoidFogYFactor() {
		return 1.0;
	}
}
