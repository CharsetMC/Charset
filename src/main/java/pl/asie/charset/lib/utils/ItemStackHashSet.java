package pl.asie.charset.lib.utils;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackHashSet extends TCustomHashSet<ItemStack> {
    public static class Strategy implements HashingStrategy<ItemStack> {
        private final boolean matchStackSize, matchDamage, matchNBT;

        public Strategy(boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
            this.matchStackSize = matchStackSize;
            this.matchDamage = matchDamage;
            this.matchNBT = matchNBT;
        }

        @Override
        public int computeHashCode(ItemStack object) {
            int i = Item.getIdFromItem(object.getItem());
            i = 31 * i + object.getItemDamage();
            if (object.hasTagCompound()) {
                i = 7 * i + object.getTagCompound().hashCode();
            }
            return i;
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return ItemUtils.equals(o1, o2, matchStackSize, matchDamage, matchNBT);
        }
    }

    public ItemStackHashSet(boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
        super(new Strategy(matchStackSize, matchDamage, matchNBT));
    }
}