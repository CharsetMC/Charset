package pl.asie.charset.audio.tape;

import net.minecraft.item.Item;

import pl.asie.charset.lib.ModCharsetLib;

public class ItemTape extends Item {
	public ItemTape() {
		super();
		this.setUnlocalizedName("charset.tape");
		this.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}
}
