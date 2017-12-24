package pl.asie.charset.module.laser.system;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.laser.ILaserBeam;
import pl.asie.charset.api.laser.ILaserBeamFactory;
import pl.asie.charset.api.laser.ILaserSource;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.module.laser.CharsetLaser;

public final class LaserBeamFactory implements ILaserBeamFactory {
	public static final LaserBeamFactory INSTANCE = new LaserBeamFactory();

	private LaserBeamFactory() {

	}

	@Override
	public ILaserBeam create(TileEntity tile, EnumFacing facing, LaserColor color) {
		return new LaserBeam(tile.getCapability(CharsetLaser.LASER_SOURCE, facing), tile.getWorld(), tile.getPos(), facing, color);
	}

	@Override
	public ILaserBeam create(ILaserSource source, World world, BlockPos pos, EnumFacing facing, LaserColor color) {
		return new LaserBeam(source, world, pos, facing, color);
	}
}
