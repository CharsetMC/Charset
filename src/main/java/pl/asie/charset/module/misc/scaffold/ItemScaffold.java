/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.misc.scaffold;

import com.google.common.base.MoreObjects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ItemBlockBase;

public class ItemScaffold extends ItemBlockBase {
	public ItemScaffold(BlockBase block) {
		super(block);
		setUnlocalizedName("charset.scaffold");
	}

	@Override
	public String getItemStackDisplayName(ItemStack is) {
		String lookup = "tile.charset.scaffold.format";
		TileScaffold tile = new TileScaffold();
		tile.loadFromStack(is);
		String displayName = MoreObjects.firstNonNull(tile.getPlank().getRelated("log"), tile.getPlank()).getStack().getDisplayName();
		return I18n.translateToLocalFormatted(lookup, displayName);
	}
}
