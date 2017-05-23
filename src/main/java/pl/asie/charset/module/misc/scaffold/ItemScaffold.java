package pl.asie.charset.module.misc.scaffold;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.ModCharset;

public class ItemScaffold extends ItemBlock {
	public ItemScaffold(Block block) {
		super(block);
		setUnlocalizedName("charset.scaffold");
		setCreativeTab(ModCharset.CREATIVE_TAB);
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.scaffold.format";
		TileScaffold tile = new TileScaffold();
		tile.loadFromStack(is);
		String displayName = Objects.firstNonNull(tile.getPlank().getRelated("log"), tile.getPlank()).getStack().getDisplayName();
		return I18n.translateToLocalFormatted(lookup, displayName);
	}
}
