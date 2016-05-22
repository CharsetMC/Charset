package pl.asie.charset.tweaks.shard;

import java.util.List;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ColorUtils;

/**
 * Created by asie on 1/15/16.
 */
public class ItemShard extends Item {
	public static class Color implements IItemColor {
		@Override
		public int getColorFromItemstack(ItemStack stack, int tintIndex) {
			int md = stack.getItemDamage();
			if (md == 0 || md > MAX_SHARD) {
				return 16777215;
			} else {
				float[] colors = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(md - 1));
				int r = (int) (colors[0] * 255.0f);
				int g = (int) (colors[1] * 255.0f);
				int b = (int) (colors[2] * 255.0f);
				return (r << 16) | (g << 8) | b;
			}
		}
	}

	public static final int MAX_SHARD = 16;

	public ItemShard() {
		super();
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getItemDamage() > 0 && stack.getItemDamage() <= 16) {
			return I18n.translateToLocalFormatted("item.charset.shard.colored.name", I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", stack.getItemDamage() - 1)));
		} else {
			return I18n.translateToLocal("item.charset.shard.name");
		}
	}
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.charset.shard.name";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i <= MAX_SHARD; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}
}
