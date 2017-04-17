package pl.asie.charset.pipes.pipe;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import pl.asie.charset.lib.network.PacketTile;
import pl.asie.charset.pipes.PipeUtils;

/**
 * Created by asie on 4/17/17.
 */
public class PacketPipeSyncExplosionTimer extends PacketTile {
    private boolean expTimerActive;

    public PacketPipeSyncExplosionTimer(TileEntity tile, boolean expTimerActive) {
        super(tile);
        this.expTimerActive =  expTimerActive;
    }

    public PacketPipeSyncExplosionTimer() {
        super();
    }

    @Override
    public void readData(INetHandler handler, ByteBuf buf) {
        super.readData(handler, buf);
        expTimerActive = buf.readBoolean();
    }

    @Override
    public void writeData(ByteBuf buf) {
        super.writeData(buf);
        buf.writeBoolean(expTimerActive);
    }

    @Override
    public void apply(INetHandler handler) {
        TilePipe pipe = PipeUtils.getPipe(tile);
        pipe.explosionTimer = expTimerActive ? 1 : 0;
    }

    @Override
    public boolean isAsynchronous() {
        return false;
    }
}
