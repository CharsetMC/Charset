package pl.asie.charset.api.laser;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILaserBeam {
	default boolean isValid(World world, BlockPos currentPos) {
		return isValid() && getBeamWorld() == world && getStart().equals(currentPos);
	}

	boolean isValid();
	World getBeamWorld();
	BlockPos getStart();
	BlockPos getEnd();
	LaserColor getColor();
	EnumFacing getDirection();
}
