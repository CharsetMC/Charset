package pl.asie.charset.drama;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemDramaInABottle extends Item {
	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		if (!worldIn.isRemote) {
			if (itemStackIn.hasTagCompound() && itemStackIn.getTagCompound().hasKey("drama")) {
				playerIn.addChatComponentMessage(new ChatComponentText(itemStackIn.getTagCompound().getString("drama")));
			} else {
				itemStackIn.setTagCompound(new NBTTagCompound());
				itemStackIn.getTagCompound().setString("drama", DramaGenerator.INSTANCE.createDrama());
				playerIn.addChatComponentMessage(new ChatComponentText(itemStackIn.getTagCompound().getString("drama")));
			}
		}
		return itemStackIn;
	}
}