package pl.asie.charset.lib.item;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.UtilProxyClient;

import javax.annotation.Nullable;

public class ItemBase extends Item {
	public ItemBase() {
		super();
		setCreativeTab(ModCharset.CREATIVE_TAB);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nullable
	public FontRenderer getFontRenderer(ItemStack stack) {
		return UtilProxyClient.FONT_RENDERER_FANCY;
	}
}
