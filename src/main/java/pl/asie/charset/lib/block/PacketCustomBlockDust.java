package pl.asie.charset.lib.block;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.INetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.render.ParticleBlockDustCharset;
import pl.asie.charset.lib.render.ParticleDiggingCharset;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.utils.Utils;

import java.util.Random;

public class PacketCustomBlockDust extends Packet {
    private static final Random rand = new Random();
    
    private World world;
    private BlockPos pos;
    private float posX, posY, posZ;
    private int numberOfParticles;
    private float particleSpeed;

    public PacketCustomBlockDust() {

    }

    public PacketCustomBlockDust(World world, BlockPos pos, double posX, double posY, double posZ, int numberOfParticles, float particleSpeed) {
        this.world = world;
        this.pos = pos;
        this.posX = (float) posX;
        this.posY = (float) posY;
        this.posZ = (float) posZ;
        this.numberOfParticles = numberOfParticles;
        this.particleSpeed = particleSpeed;
    }

    @Override
    public void readData(INetHandler handler, ByteBuf buf) {
        int dim = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        this.numberOfParticles = buf.readInt();
        this.posX = buf.readFloat();
        this.posY = buf.readFloat();
        this.posZ = buf.readFloat();
        this.particleSpeed = buf.readFloat();

        this.world = Utils.getLocalWorld(dim);
        this.pos = new BlockPos(x, y, z);
    }

    @Override
    public void apply(INetHandler handler) {
        TextureAtlasSprite sprite;
        int tintIndex = -1;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockBase) {
            tintIndex = ((BlockBase) state.getBlock()).getParticleTintIndex();
        }

        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        if (model instanceof IStateParticleBakedModel) {
            state = state.getBlock().getExtendedState(state.getActualState(world, pos), world, pos);
            sprite = ((IStateParticleBakedModel) model).getParticleTexture(state);
        } else {
            sprite = model.getParticleTexture();
        }

        ParticleManager manager = Minecraft.getMinecraft().effectRenderer;

        for (int i = 0; i < numberOfParticles; i++) {
            double xSpeed = rand.nextGaussian() * particleSpeed;
            double ySpeed = rand.nextGaussian() * particleSpeed;
            double zSpeed = rand.nextGaussian() * particleSpeed;

            try {
                Particle particle = new ParticleBlockDustCharset(world, posX, posY, posZ, xSpeed, ySpeed, zSpeed, state, pos, sprite, tintIndex);
                manager.addEffect(particle);
            } catch (Throwable var16) {
                ModCharset.logger.warn("Could not spawn block particle!");
                return;
            }
        }
    }

    @Override
    public void writeData(ByteBuf buf) {
        buf.writeInt(world.provider.getDimension());
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(numberOfParticles);
        buf.writeFloat(posX);
        buf.writeFloat(posY);
        buf.writeFloat(posZ);
        buf.writeFloat(particleSpeed);
    }

    @Override
    public boolean isAsynchronous() {
        return true;
    }
}
