package pl.asie.charset.api.laser;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILaserBeamFactory {
	ILaserBeam create(TileEntity tile, EnumFacing facing, LaserColor color);
	ILaserBeam create(ILaserSource source, World world, BlockPos pos, EnumFacing facing, LaserColor color);
}
