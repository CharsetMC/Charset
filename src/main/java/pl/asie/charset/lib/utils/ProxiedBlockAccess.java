package pl.asie.charset.lib.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class ProxiedBlockAccess implements IBlockAccess {
    public final IBlockAccess access;

    public ProxiedBlockAccess(IBlockAccess access) {
        this.access = access;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return access.getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return access.getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return access.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return access.isAirBlock(pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return access.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return access.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return access.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return access.isSideSolid(pos, side, _default);
    }
}
