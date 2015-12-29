package pl.asie.charset.gates;

import net.minecraft.item.Item;

import pl.asie.charset.lib.ModCharsetLib;

public class ItemScrewdriver extends Item {
    public ItemScrewdriver() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.screwdriver");
    }
}
