package pl.asie.charset.api.lib;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Capability for usage by tile entities which can be rotated
 * **specifically** around a given axis. If you're implementing the
 * interface, please ensure that - to the best of my knowledge, Forge
 * does not make such promises in rotateBlock()!
 *
 * For blocks, you may implement IAxisRotatable.IAxisRotatableBlock instead.
 *
 * Note that the order of checking for the block and tile implementations
 * is not defined - do not make two incompatible implementations!
 */
public interface IAxisRotatable {
	interface IAxisRotatableBlock {
		/**
		 * Rotate clockwise around the given axis.
		 * @param world The world.
		 * @param pos The block position.
		 * @param axis The axis.
		 * @return Whether the rotation was successful.
		 */
		boolean rotateAround(World world, BlockPos pos, EnumFacing axis);
	}

	/**
	 * Rotate clockwise around the given axis.
	 * @param axis The axis.
	 * @return Whether the rotation was successful.
	 */
	boolean rotateAround(EnumFacing axis);
}
