package pl.asie.charset.lib.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ParticleDiggingCharset extends ParticleDigging {
	private final IBlockState sourceState;
	private final int particleTintIndex;

	public ParticleDiggingCharset(World worldIn, double xIn, double yIn, double zIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, BlockPos pos, TextureAtlasSprite sprite, int particleTintIndex) {
		super(worldIn, xIn, yIn, zIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
		this.sourceState = state;
		this.particleTintIndex = particleTintIndex;
		setBlockPos(pos);
		setParticleTexture(sprite);
	}

	@Override
	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		if (particleTintIndex >= 0) {
			int i = Minecraft.getMinecraft().getBlockColors().colorMultiplier(this.sourceState, this.world, p_187154_1_, particleTintIndex);
			if (i != -1) {
				this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
				this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
				this.particleBlue *= (float) (i & 255) / 255.0F;
			}
		}
	}
}
