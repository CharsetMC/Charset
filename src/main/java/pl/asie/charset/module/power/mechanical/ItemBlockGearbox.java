/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

public class ItemBlockGearbox extends ItemBlockBase {
	public ItemBlockGearbox(Block block) {
		super(block);
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		ItemMaterial mat = ItemMaterialRegistry.INSTANCE.getMaterial(is.getTagCompound(), "wood");
		if (mat != null && mat.getRelated("log") != null) {
			mat = mat.getRelated("log");
		}

		if (mat != null) {
			return I18n.translateToLocalFormatted("tile.charset.gearbox.format", mat.getStack().getDisplayName());
		} else {
			return I18n.translateToLocalFormatted("tile.charset.gearbox.name");
		}
	}
}
