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

package pl.asie.simplelogic;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import pl.asie.charset.ModCharset;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.wires.SimpleLogicWires;

@Mod(modid = "simplelogic", name = "SimpleLogic", version = "@VERSION@", dependencies = "required-after:charset")
public class ModSimpleLogic {
	public static CreativeTabs CREATIVE_TAB = new CreativeTabs("simplelogic") {
		@Override
		public ItemStack getTabIconItem() {
			if (SimpleLogicWires.wireItems[17] != null) {
				return new ItemStack(SimpleLogicWires.wireItems[17]);
			} else if (SimpleLogicWires.wireItems[1] != null) {
				return new ItemStack(SimpleLogicWires.wireItems[1]);
			} else if (SimpleLogicWires.wireItems[0] != null) {
				return new ItemStack(SimpleLogicWires.wireItems[1]);
			} else if (SimpleLogicGates.gateStacks.size() > 0) {
				return SimpleLogicGates.gateStacks.iterator().next();
			} else {
				return new ItemStack(Items.REDSTONE);
			}
		}
	};
}
