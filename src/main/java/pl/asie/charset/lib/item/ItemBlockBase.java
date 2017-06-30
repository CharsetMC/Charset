package pl.asie.charset.lib.item;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.UtilProxyClient;

import javax.annotation.Nullable;

public class ItemBlockBase extends ItemBlock {
	public ItemBlockBase(Block block) {
		super(block);
		setCreativeTab(ModCharset.CREATIVE_TAB);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nullable
	public FontRenderer getFontRenderer(ItemStack stack) {
		return UtilProxyClient.FONT_RENDERER_FANCY;
	}
}
