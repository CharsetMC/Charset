package pl.asie.charset.lib.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SubItemProviderSimple implements ISubItemProvider {
    private final List<ItemStack> items;

    public SubItemProviderSimple(List<ItemStack> items) {
        this.items = items;
    }

    public SubItemProviderSimple(Item item) {
        this.items = Collections.singletonList(new ItemStack(item));
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }
}
