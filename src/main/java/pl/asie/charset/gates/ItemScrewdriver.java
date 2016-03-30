package pl.asie.charset.gates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import pl.asie.charset.lib.ModCharsetLib;

public class ItemScrewdriver extends Item {
	public ItemScrewdriver() {
		super();
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		setUnlocalizedName("charset.screwdriver");
		setHarvestLevel("screwdriver", 2);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
}
