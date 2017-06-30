package pl.asie.charset.lib.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;

import java.util.*;

public class SubItemProviderSets implements ISubItemProvider {
    protected Collection<ItemStack> createForcedItems() {
        return Collections.emptyList();
    }

    protected List<Collection<ItemStack>> createItemSets() {
        return Collections.emptyList();
    }

    protected int getVisibleSetAmount() {
        return 1;
    }

    private Collection<ItemStack> getItems(boolean all) {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        builder.addAll(createForcedItems());
        List<Collection<ItemStack>> sets = createItemSets();

        if (sets.size() > 0) {
            if (all || ModCharset.INDEV || getVisibleSetAmount() >= sets.size()) {
                for (Collection<ItemStack> set : sets)
                    builder.addAll(set);
            } else {
                Calendar cal = CharsetLib.calendar.get();
                int doy = (cal.get(Calendar.YEAR) * 366) + cal.get(Calendar.DAY_OF_YEAR) - 1 /* start at 0, not 1 */;
                Collections.shuffle(sets, new Random(doy));
                for (int i = 0; i < Math.min(getVisibleSetAmount(), sets.size()); i++)
                    builder.addAll(sets.get(i));
            }
        }

        return builder.build();
    }

    @Override
    public Collection<ItemStack> getItems() {
        return getItems(false);
    }

    @Override
    public Collection<ItemStack> getAllItems() {
        return getItems(true);
    }
}
