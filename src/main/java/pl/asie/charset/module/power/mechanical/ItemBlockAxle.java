package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

public class ItemBlockAxle extends ItemBlockBase {
	public ItemBlockAxle(Block block) {
		super(block);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		Block block = worldIn.getBlockState(pos).getBlock();

		if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
			side = EnumFacing.UP;
		} else if (!block.isReplaceable(worldIn, pos)) {
			pos = pos.offset(side);
		}

		IBlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if (state.getBlock() instanceof BlockAxle) {
			TileEntity tile = worldIn.getTileEntity(pos.offset(side.getOpposite()));
			if (tile instanceof TileAxle) {
				EnumFacing.Axis axis = state.getValue(Properties.AXIS);
				ItemMaterial other = ((TileAxle) tile).getMaterial();
				if (axis == side.getAxis() && other == ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material", "plank")) {
					return true;
				} else {
					return false;
				}
			}
		}

		return worldIn.mayPlace(this.block, pos, false, side, (Entity)null);
	}
}
