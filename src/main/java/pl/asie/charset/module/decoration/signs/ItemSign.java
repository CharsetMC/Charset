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

package pl.asie.charset.module.decoration.signs;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.*;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.storage.chests.CharsetStorageChests;

import java.util.List;
import java.util.Optional;

public class ItemSign extends ItemBase {
	@SideOnly(Side.CLIENT)
	public static class Color implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if (tintIndex == 0 && stack.hasTagCompound()) {
				ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material", "plank");
				if (material != null) {
					return RenderUtils.getAverageColor(
							RenderUtils.getItemSprite(material.getStack()),
							RenderUtils.AveragingMode.FULL
					);
				}
			}

			return -1;
		}
	}

	public ItemSign() {
		super();
		setMaxStackSize(16);
		setTranslationKey("sign");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		Optional<String> s = ItemMaterialRegistry.INSTANCE.getLocalizedNameFor(
				ItemMaterialRegistry.INSTANCE.getMaterial(is.getTagCompound(), "material")
		);

		String baseName = I18n.translateToLocal(getTranslationKey(is) + ".name");

		return s.map(s1 -> I18n.translateToLocalFormatted("tile.charset.sign.format", s1, baseName)).orElse(baseName);
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetDecorationSigns.itemSign) {
			@Override
			protected int compareSets(List<ItemStack> first, List<ItemStack> second) {
				return SubItemSetHelper.wrapLists(first, second, SubItemSetHelper.extractMaterial("material", SubItemSetHelper::sortByItem));
			}
		});
	}
}
