package pl.asie.charset.module.misc.scaffold;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.item.ItemBlockBase;

public class ItemScaffold extends ItemBlockBase {
	public ItemScaffold(Block block) {
		super(block);
		setUnlocalizedName("charset.scaffold");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.scaffold.format";
		TileScaffold tile = new TileScaffold();
		tile.loadFromStack(is);
		String displayName = MoreObjects.firstNonNull(tile.getPlank().getRelated("log"), tile.getPlank()).getStack().getDisplayName();
		return I18n.translateToLocalFormatted(lookup, displayName);
	}
}
