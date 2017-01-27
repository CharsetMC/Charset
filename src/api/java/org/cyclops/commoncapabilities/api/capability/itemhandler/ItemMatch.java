package org.cyclops.commoncapabilities.api.capability.itemhandler;

import net.minecraft.item.ItemStack;

/**
 * Item matching flags to be used in {@link ISlotlessItemHandler}.
 * @author rubensworks
 */
public final class ItemMatch {

    /**
     * Convenience value matching ItemStacks only by Item.
     */
    public static final int ANY = 0;
    /**
     * Match ItemStack damage values.
     */
    public static final int DAMAGE = 1;
    /**
     * Match ItemStack NBT tags.
     */
    public static final int NBT = 2;
    /**
     * Match ItemStack stacksizes.
     */
    public static final int STACKSIZE = 4;
    /**
     * Convenience value matching ItemStacks exactly by damage value, NBT tag and stacksize.
     */
    public static final int EXACT = DAMAGE | NBT | STACKSIZE;

    public static boolean areItemStacksEqual(ItemStack a, ItemStack b, int matchFlags) {
        boolean damage    = (matchFlags & DAMAGE   ) > 0;
        boolean nbt       = (matchFlags & NBT      ) > 0;
        boolean stackSize = (matchFlags & STACKSIZE) > 0;
        return a == b ||
                (!a.isEmpty() && !b.isEmpty()
                        && a.getItem() == b.getItem()
                        && (!damage || a.getItemDamage() == b.getItemDamage())
                        && (!stackSize || a.getCount() == b.getCount())
                        && (!nbt || ItemStack.areItemStackTagsEqual(a, b)));
    }

}
