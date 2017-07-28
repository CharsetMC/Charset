package pl.asie.charset.lib.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleBlockDustCharset extends ParticleDiggingCharset {
    public ParticleBlockDustCharset(World worldIn, double xIn, double yIn, double zIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, BlockPos pos, TextureAtlasSprite sprite, int particleTintIndex) {
        super(worldIn, xIn, yIn, zIn, xSpeedIn, ySpeedIn, zSpeedIn, state, pos, sprite, particleTintIndex);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
    }
}
