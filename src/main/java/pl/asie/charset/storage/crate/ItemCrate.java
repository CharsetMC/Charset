package pl.asie.charset.storage.crate;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.blocks.ItemBlockBase;

public class ItemCrate extends ItemBlockBase {
    public ItemCrate(Block block) {
        super(block);
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.crate");
    }

    @Override
    public String getItemStackDisplayName(ItemStack is) {
        TileEntityCrate crate = new TileEntityCrate();
        crate.loadFromStack(is);
        return I18n.translateToLocalFormatted("tile.charset.crate.format", crate.getMaterial().getDisplayName());
    }

}
