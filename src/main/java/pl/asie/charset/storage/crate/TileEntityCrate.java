package pl.asie.charset.storage.crate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pl.asie.charset.lib.TileBase;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.storage.ModCharsetStorage;

public class TileEntityCrate extends TileBase {
    private ItemStack plank;

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        loadFromStack(stack);
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        plank = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("plank"));

        if (plank == null) {
            plank = new ItemStack(Blocks.PLANKS);
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        ItemUtils.writeToNBT(plank, compound, "plank");
        return compound;
    }

    public static ItemStack create(ItemStack plank) {
        TileEntityCrate crate = new TileEntityCrate();
        crate.plank = plank;
        return crate.getDroppedBlock();
    }

    public ItemStack getDroppedBlock() {
        ItemStack stack = new ItemStack(ModCharsetStorage.crateBlock);
        writeNBTData(ItemUtils.getTagCompound(stack, true), true);
        return stack;
    }

    public void loadFromStack(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        readNBTData(compound != null ? stack.getTagCompound() : new NBTTagCompound(), true);
    }

    public CrateCacheInfo getCacheInfo() {
        return new CrateCacheInfo(plank, (byte) 0x3f);
    }
}
