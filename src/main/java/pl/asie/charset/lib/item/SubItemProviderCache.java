package pl.asie.charset.lib.item;

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SubItemProviderCache implements ISubItemProvider {
    private static final Set<SubItemProviderCache> CACHES = new HashSet<>();
    private final ISubItemProvider parent;
    private Collection<ItemStack> items;

    public SubItemProviderCache(ISubItemProvider parent) {
        CACHES.add(this);
        this.parent = parent;
    }

    public static void clear() {
        for (SubItemProviderCache cache : CACHES) {
            cache.items = null;
        }
    }

    @Override
    public Collection<ItemStack> getItems() {
        if (items != null)
            return items;

        Collection<ItemStack> genItems = parent.getItems();
        items = genItems;
        return items;
    }

    @Override
    public Collection<ItemStack> getAllItems() {
        return parent.getAllItems();
    }
}
