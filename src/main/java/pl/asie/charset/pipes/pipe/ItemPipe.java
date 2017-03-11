package pl.asie.charset.pipes.pipe;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.misc.shelf.TileShelf;

public class ItemPipe extends ItemBlock {
	public ItemPipe(Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("charset.pipe");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.pipe.format";
		String displayName = ItemMaterialRegistry.INSTANCE.getMaterial(is.getTagCompound(), "material", "stone", new ItemStack(Blocks.STONE)).getStack().getDisplayName();
		return I18n.translateToLocalFormatted(lookup, displayName);
	}
}
