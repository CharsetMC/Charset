package pl.asie.charset.misc.shelf;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

public class ItemShelf extends ItemBlock {
	public ItemShelf(Block block) {
		super(block);
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
