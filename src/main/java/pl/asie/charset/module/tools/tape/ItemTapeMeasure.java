package pl.asie.charset.module.tools.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.item.ItemBase;

public class ItemTapeMeasure extends ItemBase {
	public ItemTapeMeasure() {
		super();
		setUnlocalizedName("charset.tapeMeasure");
		setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		{
			EnumActionResult oldResult;
			if ((oldResult = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)) != EnumActionResult.PASS) {
				return oldResult;
			}
		}

		System.out.println("Used");
		return EnumActionResult.SUCCESS;
	}
}
