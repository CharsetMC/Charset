package pl.asie.charset.module.crafting.compression.grid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GridEntry {
	private final World world;
	private final BlockPos pos;

	public GridEntry(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	public World getWorld() {
		return world;
	}

	public BlockPos getPos() {
		return pos;
	}

	public IBlockState getState() {
		return world.getBlockState(pos);
	}

	public TileEntity getTileEntity() {
		return world.getTileEntity(pos);
	}

	public abstract boolean isInvalid();
	public abstract ItemStack getCraftingStack();
	public abstract ItemStack mergeRemainingItem(ItemStack target, boolean simulate);
}
