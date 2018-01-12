package pl.asie.charset.patchwork;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.utils.ThreeState;

public class LocksCapabilityHook {
	public interface Handler {
		boolean blocksCapability(TileEntity tile, Capability capability, EnumFacing facing);
	}

	public static Handler handler = (tile, capability, facing) -> false;
}
