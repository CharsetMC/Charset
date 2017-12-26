package pl.asie.charset.lib.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!world.setBlockState(pos, newState, 3)) {
			return false;
		}

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this.block) {
			setTileEntityNBT(world, player, pos, stack);
			((BlockBase) this.block).onBlockPlacedBy(world, pos, state, player, stack, side, hitX, hitY, hitZ);
		}

		return true;
	}
}
