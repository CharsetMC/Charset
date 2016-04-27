package pl.asie.charset.storage.locking;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pl.asie.charset.api.storage.IKeyItem;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemMasterKey extends Item implements IKeyItem {
    public ItemMasterKey() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.masterKey");
    }

    @Override
    public boolean canUnlock(String lock, ItemStack stack) {
        return true;
    }
}
