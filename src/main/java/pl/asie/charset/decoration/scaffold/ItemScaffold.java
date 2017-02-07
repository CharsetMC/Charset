package pl.asie.charset.decoration.scaffold;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.storage.barrel.TileEntityDayBarrel;

public class ItemScaffold extends ItemBlock {
	public ItemScaffold(Block block) {
		super(block);
		setUnlocalizedName("charset.scaffold");
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.scaffold.format";
		TileScaffold tile = new TileScaffold();
		tile.loadFromStack(is);
		return I18n.translateToLocalFormatted(lookup, tile.getPlank().getStack().getDisplayName());
	}
}
