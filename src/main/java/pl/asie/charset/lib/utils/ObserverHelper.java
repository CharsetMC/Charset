package pl.asie.charset.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

public final class ObserverHelper {
	private ObserverHelper() {

	}

	public static void updateObservingBlocksAt(World world, BlockPos pos, Block type) {
		world.updateObservingBlocksAt(pos, type);
	}

	public static void updateObservingBlocksAt(World world, BlockPos pos, Block type, BiPredicate<BlockPos, EnumFacing> predicate) {
		notifyObserverChanged(world, pos.down(), pos, EnumFacing.DOWN, type, predicate);
		notifyObserverChanged(world, pos.up(), pos, EnumFacing.UP, type, predicate);
		notifyObserverChanged(world, pos.north(), pos, EnumFacing.NORTH, type, predicate);
		notifyObserverChanged(world, pos.south(), pos, EnumFacing.SOUTH, type, predicate);
		notifyObserverChanged(world, pos.west(), pos, EnumFacing.WEST, type, predicate);
		notifyObserverChanged(world, pos.east(), pos, EnumFacing.EAST, type, predicate);
	}

	public static void notifyObserverChanged(World world, BlockPos pos, EnumFacing facing, Block type) {
		world.observedNeighborChanged(pos.offset(facing), type, pos);
	}

	public static void notifyObserverChanged(World world, BlockPos pos, BlockPos origPos, Block type) {
		world.observedNeighborChanged(pos, type, origPos);
	}

	public static void notifyObserverChanged(World world, BlockPos pos, BlockPos origPos, EnumFacing facing, Block type, BiPredicate<BlockPos, EnumFacing> predicate) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == Blocks.OBSERVER && predicate.test(pos, facing)) {
			try {
				((BlockObserver) state.getBlock()).observedNeighborChanged(state, world, origPos, type, pos);
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
				CrashReportCategory crashreportcategory = crashreport.makeCategory("IAxisRotatableBlock being updated");
				crashreportcategory.addDetail("Source block type", new ICrashReportDetail<String>() {
					public String call() throws Exception {
						try {
							return String.format("ID #%d (%s // %s)", new Object[] {Integer.valueOf(Block.getIdFromBlock(type)), type.getUnlocalizedName(), type.getClass().getCanonicalName()});
						} catch (Throwable var2) {
							return "ID #" + Block.getIdFromBlock(type);
						}
					}
				});
				CrashReportCategory.addBlockInfo(crashreportcategory, pos, state);
				throw new ReportedException(crashreport);
			}
		}
	}
}
