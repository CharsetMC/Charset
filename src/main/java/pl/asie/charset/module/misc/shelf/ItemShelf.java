package pl.asie.charset.module.misc.shelf;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ItemBlockBase;

public class ItemShelf extends ItemBlockBase {
	public ItemShelf(BlockBase block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("charset.shelf");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.shelf.format";
		TileShelf tile = new TileShelf();
		tile.loadFromStack(is);
		String displayName = MoreObjects.firstNonNull(tile.getPlank().getRelated("log"), tile.getPlank()).getStack().getDisplayName();
		return I18n.translateToLocalFormatted(lookup, displayName);
	}
}
