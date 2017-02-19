package pl.asie.charset.lib.wires;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWireContainer {
    World world();
    BlockPos pos();
    void requestNeighborUpdate(int connectionMask);
    void requestNetworkUpdate();
    void requestRenderUpdate();

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
    }
}
