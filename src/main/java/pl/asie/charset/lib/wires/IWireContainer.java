package pl.asie.charset.lib.wires;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWireContainer {
    World world();
    BlockPos pos();
    void requestNeighborUpdate(int connectionMask);
    default void requestNeighborUpdate(EnumFacing facing) {
        requestNeighborUpdate(1 << (facing.ordinal() + 8));
    }
    void requestNetworkUpdate();
    void requestRenderUpdate();
    void dropWire();

    class Dummy implements IWireContainer {
        @Override
        public World world() {
            return null;
        }

        @Override
        public BlockPos pos() {
            return null;
        }

        @Override
        public void requestNeighborUpdate(int connectionMask) {

        }

        @Override
        public void requestNetworkUpdate() {

        }

        @Override
        public void requestRenderUpdate() {

        }

        @Override
        public void dropWire() {

        }
    }
}
