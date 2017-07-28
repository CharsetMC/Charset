package pl.asie.charset.lib.capability;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileCache {
    protected final World world;
    protected final BlockPos pos;
    protected IBlockState state;
    protected boolean hasTile;
    private TileEntity tile;

    public TileCache(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public void neighborChanged(BlockPos pos) {
        if (this.pos.equals(pos)) {
            state = null;
        }
    }

    public void reload() {
        state = world.getBlockState(pos);
        hasTile = state.getBlock().hasTileEntity(state);
        tile = null;
    }

    @Nonnull
    public IBlockState getBlock() {
        if (state == null || (hasTile && tile.isInvalid())) {
            reload();
        }

        return state;
    }

    @Nullable
    public TileEntity getTile() {
        if (state == null || (tile != null && tile.isInvalid())) {
            reload();
        }

        if (hasTile) {
            if (tile == null) {
                tile = world.getTileEntity(pos);
            }
            return tile;
        } else {
            return null;
        }
    }
}
