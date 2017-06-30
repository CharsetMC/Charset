package pl.asie.charset.lib.item;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.UtilProxyClient;

import javax.annotation.Nullable;

public class ItemBase extends Item {
	private final ISubItemProvider subItemProvider;

	public ItemBase() {
		super();
		setCreativeTab(ModCharset.CREATIVE_TAB);
		subItemProvider = createSubItemProvider();
	}

	protected ISubItemProvider createSubItemProvider() {
		return new SubItemProviderSimple(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nullable
	public FontRenderer getFontRenderer(ItemStack stack) {
		return UtilProxyClient.FONT_RENDERER_FANCY;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.addAll(subItemProvider.getItems());
		}
	}
}
