/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.materials.shards;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.ColorUtils;

public class ItemGlassShard extends ItemBase {
	@SideOnly(Side.CLIENT)
	public static class Color implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			int md = stack.getItemDamage();
			if (md == 0 || md > MAX_SHARD) {
				return -1;
			} else {
				return ColorUtils.toIntColor(EnumDyeColor.byMetadata(md - 1));
			}
		}
	}

	public static final int MAX_SHARD = 16;

	public ItemGlassShard() {
		super();
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getItemDamage() > 0 && stack.getItemDamage() <= 16) {
			return I18n.translateToLocalFormatted("item.charset.shard.colored.name", I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", EnumDyeColor.byMetadata(stack.getItemDamage() - 1))));
		} else {
			return I18n.translateToLocal("item.charset.shard.name");
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return "item.charset.shard.name";
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i <= MAX_SHARD; i++) {
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}
}
