package pl.asie.charset.wires;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import pl.asie.charset.wires.internal.WireLocation;

public final class WireUtils {
	private static final Set<Block> WIRE_PLACEABLE = new HashSet<Block>();

	static {
		WIRE_PLACEABLE.add(Blocks.glowstone);
		WIRE_PLACEABLE.add(Blocks.piston);
		WIRE_PLACEABLE.add(Blocks.sticky_piston);
	}

	private WireUtils() {

	}

	public static float getWireHitboxHeight(TileWireContainer tile, WireLocation loc) {
		switch (tile.getWireType(loc).type()) {
			case NORMAL:
				return 0.125f;
			case INSULATED:
				return 0.1875f;
			case BUNDLED:
				return 0.25f;
		}

		return 0.125f;
	}

	public static boolean canPlaceWire(World world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (WIRE_PLACEABLE.contains(state.getBlock())) {
			return true;
		}

		return block.isSideSolid(world, pos, side);
	}
}
