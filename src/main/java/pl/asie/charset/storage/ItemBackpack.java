package pl.asie.charset.storage;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;
import pl.asie.charset.storage.gui.SlotArmorBackpack;

public class ItemBackpack extends ItemBlock {
    public class InventoryOwnerBackpack implements IInventoryOwner {
        public final ItemStack stack;

        protected InventoryOwnerBackpack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void onInventoryChanged(IInventory inventory) {
            ((InventorySimple) inventory).writeToNBT(stack.getTagCompound(), "items");
        }
    }

    public ItemBackpack(Block block) {
        super(block);
    }

    @Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
        return armorType == 1;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof EntityPlayer) {
            Container container = ((EntityPlayer) entity).inventoryContainer;
            Slot slot = container.getSlot(6);
            if (!(slot instanceof SlotArmorBackpack)) {
                container.inventorySlots.set(6, new SlotArmorBackpack((EntityPlayer) entity, ((EntityPlayer) entity).inventory,
                        slot.getSlotIndex(), slot.xDisplayPosition, slot.yDisplayPosition));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return BlockBackpack.DEFAULT_COLOR;
    }

    public IInventory getInventory(ItemStack stack) {
        if (stack.getItem() == this) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }

            InventorySimple inventory = new InventorySimple(27, new InventoryOwnerBackpack(stack));
            inventory.readFromNBT(stack.getTagCompound(), "items");
            return inventory;
        } else {
            return null;
        }
    }
}
