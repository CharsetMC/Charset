package pl.asie.charset.lib.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Collections;

public class SubItemProviderSimple implements ISubItemProvider {
    private final Collection<ItemStack> items;

    public SubItemProviderSimple(Collection<ItemStack> items) {
        this.items = items;
    }

    public SubItemProviderSimple(Item item) {
        this.items = Collections.singletonList(new ItemStack(item));
    }

    @Override
    public Collection<ItemStack> getItems() {
        return items;
    }
}
