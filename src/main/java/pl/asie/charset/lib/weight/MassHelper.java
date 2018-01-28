package pl.asie.charset.lib.weight;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.utils.SpaceUtils;

public class MassHelper {
	public static MassHelper INSTANCE = new MassHelper();

	protected MassHelper() {

	}

	public double getDensity(World world, BlockPos pos) {
		return getDensity(world, pos, world.getBlockState(pos));
	}

	public double getDensity(World world, BlockPos pos, IBlockState state) {
		return state.getBlockHardness(world, pos);
	}

	public double getMass(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		double density = getDensity(world, pos, state);
		AxisAlignedBB box = state.getCollisionBoundingBox(world, pos);
		if (box == null) {
			return 0;
		} else {
			return SpaceUtils.getVolume(box) * density;
		}
	}
}
