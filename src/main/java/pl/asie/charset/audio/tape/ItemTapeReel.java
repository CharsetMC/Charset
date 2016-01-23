package pl.asie.charset.audio.tape;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTapeReel extends Item {
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		int i = 1;
		while (i <= 128) {
			subItems.add(new ItemStack(itemIn, 1, i));
			i <<= 1;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		int mins = stack.getItemDamage() >> 2;
		int secs = (stack.getItemDamage() & 3) * 15;
		TapeUtils.addTooltip(tooltip, mins, secs);
	}
}
