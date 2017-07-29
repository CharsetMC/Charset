package pl.asie.charset.lib.capability.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.storage.IBarrel;

public class DummyBarrel implements IBarrel {
    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public int getMaxItemCount() {
        return 0;
    }

    @Override
    public boolean containsUpgrade(String upgradeName) {
        return false;
    }

    @Override
    public boolean shouldExtractFromSide(EnumFacing side) {
        return false;
    }

    @Override
    public boolean shouldInsertToSide(EnumFacing side) {
        return false;
    }

    @Override
    public ItemStack extractItem(int maxCount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return stack;
    }
}
