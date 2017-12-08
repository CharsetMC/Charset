package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.utils.ColorUtils;

public class ItemBlockTank extends ItemBlockBase {
	public ItemBlockTank(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return 0;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().getInteger("color") >= 0) {
			return I18n.translateToLocalFormatted("tile.charset.tank.colored.name", I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", EnumDyeColor.byMetadata(stack.getTagCompound().getInteger("color")))));
		} else {
			return I18n.translateToLocal("tile.charset.tank.name");
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.charset.tank.name";
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (TileTank.checkPlacementConflict(worldIn.getTileEntity(pos.down()), worldIn.getTileEntity(pos.up()), (stack.getTagCompound().getInteger("color") + 1) % 17)) {
			return false;
		} else {
			return super.placeBlockAt(stack, player, worldIn, pos, side, hitX, hitY, hitZ, newState);
		}
	}
}
