package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.lib.ModCharsetLib;

public class AudioSinkBlock extends AudioSink {
    private World world;
    private Vec3d pos;

    public AudioSinkBlock() {

    }

    public AudioSinkBlock(World world, BlockPos pos) {
        this.world = world;
        this.pos = new Vec3d(pos);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vec3d getPos() {
        return pos;
    }

    @Override
    public float getDistance() {
        return 32.0F;
    }

    @Override
    public float getVolume() {
        return 1.0F;
    }

    @Override
    public void writeData(ByteBuf buf) {
        super.writeData(buf);
        buf.writeInt(world.provider.getDimension());
        buf.writeDouble(pos.xCoord);
        buf.writeDouble(pos.yCoord);
        buf.writeDouble(pos.zCoord);
    }

    @Override
    public void readData(ByteBuf buf) {
        super.readData(buf);
        int dimId = buf.readInt();
        double xPos = buf.readDouble();
        double yPos = buf.readDouble();
        double zPos = buf.readDouble();

        world = ModCharsetLib.proxy.getLocalWorld(dimId);
        pos = new Vec3d(xPos, yPos, zPos);
    }
}
