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

package pl.asie.charset.module.tablet.format.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordItem;

import java.util.ArrayList;
import java.util.List;

public class CommandItem implements ICommand {
	@Override
	public void call(ITypesetter typesetter, ITokenizer tokenizer) throws TruthError {
		String itemName = tokenizer.getParameter("No item specified");
		List<ItemStack> items = new ArrayList<>();
		Item i = Item.getByNameOrId(itemName);

		int stackSize = 1;
		int dmg = 0;

		String stackSizeS = tokenizer.getOptionalParameter();

		if (stackSizeS != null) {
			stackSize = Integer.parseInt(stackSizeS);
			String dmgS = tokenizer.getOptionalParameter();
			if (dmgS != null) {
				dmg = Integer.parseInt(dmgS);
			}
		}

		if (i == null) {
			items.addAll(OreDictionary.getOres(itemName, false));
		} else {
			ItemStack stack = new ItemStack(i, stackSize, dmg);
			items.add(stack);
		}

		typesetter.write(new WordItem(items));
	}
}
