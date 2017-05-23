package pl.asie.charset.module.misc.shelf;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class ItemShelf extends ItemBlock {
	public ItemShelf(Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("charset.shelf");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.shelf.format";
		TileShelf tile = new TileShelf();
		tile.loadFromStack(is);
		String displayName = Objects.firstNonNull(tile.getPlank().getRelated("log"), tile.getPlank()).getStack().getDisplayName();
		return I18n.translateToLocalFormatted(lookup, displayName);
	}
}
