package pl.asie.charset.module.tablet.format.words;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.module.tablet.format.api.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WordItem extends Word {
    private final ItemStack errorStack = new ItemStack(Blocks.FIRE);
    private final List<ItemStack> entries;
    private final float scale;

    public WordItem(Collection<ItemStack> entries) {
        this(entries, 1.0f);
    }

    public WordItem(Collection<ItemStack> entries, float scale) {
        this.entries = expand(entries);
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    private List<ItemStack> expand(Collection<ItemStack> stacks) {
        List<ItemStack> newList = new ArrayList<>();

        for (ItemStack is : stacks) {
            if (is.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                NonNullList<ItemStack> out = NonNullList.create();
                is.getItem().getSubItems(CreativeTabs.SEARCH, out);
                newList.addAll(expand(out));
            } else {
                newList.add(is);
            }
        }

        return newList;
    }

    private int activeItemIndex;

    public ItemStack getItem() {
        activeItemIndex = 0;
        if (entries.size() == 0) {
            return errorStack;
        }

        long now = System.currentTimeMillis() / 1000;
        now %= entries.size();
        activeItemIndex = (int) now;
        return entries.get(activeItemIndex);
    }

    public void onItemErrored(Throwable t) {
        t.printStackTrace();
        if (entries != null && activeItemIndex < entries.size()) {
            entries.set(activeItemIndex, errorStack);
        }
    }
}
