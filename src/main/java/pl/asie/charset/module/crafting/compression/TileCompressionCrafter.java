package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ItemUtils;

public class TileCompressionCrafter extends TileBase {
	protected CompressionShape shape;
	protected boolean redstoneLevel;

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

	private void getShape() {
		if (shape != null && shape.isInvalid()) {
			shape = null;
		}

		if (shape == null) {
			shape = CompressionShape.build(world, pos);
			if (shape == null) {
				new Notice(this, new TextComponentString("Invalid shape")).sendToAll();
			}
		}
	}

	public void craft(IBlockState state) {
		ItemStack result = shape.craft(pos, state.getValue(Properties.FACING), false);
	}

	public void onNeighborChange(IBlockState state) {
		if (!world.isRemote) {
			redstoneLevel = false;
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (facing == state.getValue(Properties.FACING).getOpposite()) {
					continue;
				}
				if (world.getRedstonePower(pos, state.getValue(Properties.FACING)) > 0) {
					redstoneLevel = true;
					break;
				}
			}
			if (redstoneLevel) {
				getShape();
				if (shape != null) {
					craft(state);
				}
			} else {
				getShape();
				if (shape != null) {
					shape.checkRedstoneLevels();
				}
			}
		}
	}
}
