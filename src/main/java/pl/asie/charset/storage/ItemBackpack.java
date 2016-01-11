package pl.asie.charset.storage;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.inventory.IInventoryOwner;

public class ItemBackpack extends ItemBlock implements IInventoryOwner {
    public ItemBackpack(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return BlockBackpack.DEFAULT_COLOR;
    }

    @Override
    public void onInventoryChanged(IInventory inventory) {

    }
}
