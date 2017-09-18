package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.utils.ColorUtils;

public class ItemBlockTank extends ItemBlockBase {
	public ItemBlockTank(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return 0;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getItemDamage() > 0 && stack.getItemDamage() <= 16) {
			return I18n.translateToLocalFormatted("tile.charset.tank.colored.name", I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", EnumDyeColor.byMetadata(stack.getItemDamage() - 1))));
		} else {
			return I18n.translateToLocal("tile.charset.tank.name");
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.charset.tank.name";
	}
}
