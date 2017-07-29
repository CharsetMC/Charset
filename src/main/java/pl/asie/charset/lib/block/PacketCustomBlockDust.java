package pl.asie.charset.lib.block;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.UtilProxyCommon;
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
        UtilProxyCommon.proxy.spawnBlockDustClient(world, pos, rand, posX, posY, posZ, numberOfParticles, particleSpeed);
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
        return false;
    }
}
