/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.misc.shards;

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

public class ItemShard extends ItemBase {
	@SideOnly(Side.CLIENT)
	public static class Color implements IItemColor {
		@Override
		public int getColorFromItemstack(ItemStack stack, int tintIndex) {
			int md = stack.getItemDamage();
			if (md == 0 || md > MAX_SHARD) {
				return 16777215;
			} else {
				return ColorUtils.toIntColor(EnumDyeColor.byMetadata(md - 1));
			}
		}
	}

	public static final int MAX_SHARD = 16;

	public ItemShard() {
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
	public String getUnlocalizedName(ItemStack stack) {
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
