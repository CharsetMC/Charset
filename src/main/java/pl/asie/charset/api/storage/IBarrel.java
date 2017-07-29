package pl.asie.charset.api.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IBarrel {
    int getItemCount();
    int getMaxItemCount();
    boolean containsUpgrade(String upgradeName);

    // The following indicate behaviour of barrel IItemHandlers.
    // Please respect them if applicable, though they are not
    // enforced.
    boolean shouldExtractFromSide(EnumFacing side);
    boolean shouldInsertToSide(EnumFacing side);

    // The following two methods follow the IItemHandler contract,
    // except they do not cap out at the item's maximum stack size.
    ItemStack extractItem(int maxCount, boolean simulate);
    ItemStack insertItem(ItemStack stack, boolean simulate);
}
