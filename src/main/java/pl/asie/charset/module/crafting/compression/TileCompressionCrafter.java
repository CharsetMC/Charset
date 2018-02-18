package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentString;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.misc.IItemInsertionEmitter;
import pl.asie.charset.lib.notify.Notice;

// todo: make it less ITickable
public class TileCompressionCrafter extends TileBase implements ITickable, IItemInsertionEmitter {
	protected CompressionShape shape;
	protected boolean redstoneLevel;

	@Override
	public void update() {
		if (shape != null) {
			shape.tick();
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		redstoneLevel = compound.getByte("rs") > 0;
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		super.writeNBTData(compound, isClient);
		compound.setByte("rs", (byte) (redstoneLevel ? 15 : 0));
		return compound;
	}

	public void getShape(boolean warn) {
		if (shape != null && shape.isInvalid()) {
			shape = null;
		}

		if (shape == null) {
			shape = CompressionShape.build(world, pos);
			if (shape == null && warn) {
				new Notice(this, new TextComponentString("Invalid shape")).sendToAll();
			}
		}
	}

	public void craft(IBlockState state) {
		shape.craftBegin(this, state.getValue(Properties.FACING));
	}

	public void onNeighborChange(IBlockState state) {
		if (!world.isRemote) {
			redstoneLevel = world.getRedstonePower(pos, state.getValue(Properties.FACING)) > 0;
			if (redstoneLevel) {
				getShape(true);
				if (shape != null) {
					craft(state);
				}
			} else {
				getShape(false);
				if (shape != null) {
					shape.checkRedstoneLevels(true);
				}
			}
		}
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean connectsForItemInsertion(EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockCompressionCrafter) {
			return state.getValue(Properties.FACING) != side;
		} else {
			return false;
		}
	}
}
