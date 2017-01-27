package org.cyclops.commoncapabilities.api.capability.itemhandler;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * A simplified form of the {@link net.minecraftforge.items.IItemHandler} that is slot-agnostic.
 * By not taking into account slots, the item handler provider instead of the consumer
 * is responsible for providing an efficient item insertion and extraction algorithm.
 * @author rubensworks
 */
public interface ISlotlessItemHandler {

    /**
     * Inserts an ItemStack into the item handler and return the remainder.
     * The ItemStack should not be modified in this function!
     * Note: This behaviour is subtly different from IFluidHandlers.fill()
     *
     * @param stack    ItemStack to insert.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return {@link ItemStack#EMPTY}).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     **/
    @Nonnull
    ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate);

    /**
     * Extracts an ItemStack from the item handler. The returned value must be null
     * if nothing is extracted, otherwise it's stack size must not be greater than amount or the
     * itemstacks getMaxStackSize().
     *
     * @param amount   Amount to extract (may be greater than the current stacks max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be {@link ItemStack#EMPTY}, if nothing can be extracted
     **/
    @Nonnull
    ItemStack extractItem(int amount, boolean simulate);

    /**
     * Extract an ItemStack matching the given stack from the item handler.
     * If nothing is extracted, otherwise it's stack size must not be greater than the itemstacks getMaxStackSize()
     * Note: the returned ItemStack stacksize could be different (and even greater) than the matchStack
     * if the stacksize is ignored according to the matchFlags, this is different from the semantics
     * of {@link net.minecraftforge.items.IItemHandler#extractItem(int, int, boolean)}.
     *
     * @param matchStack The ItemStack to search for.
     * @param matchFlags The flags to compare the given matchStack by according to {@link ItemMatch}.
     *                   ItemMatch.DAMAGE | ItemMatch.NBT will for instance make sure to only extract
     *                   items that have exactly the same damage value and nbt tag, while ignoring the stacksize.
     * @param simulate   If true, the insertion is only simulated
     * @return ItemStack extracted from the slot, must be {@link ItemStack#EMPTY}, if nothing can be extracted
     */
    @Nonnull
    ItemStack extractItem(@Nonnull ItemStack matchStack, int matchFlags, boolean simulate);

}
