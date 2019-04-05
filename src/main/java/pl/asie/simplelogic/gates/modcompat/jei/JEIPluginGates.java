/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.simplelogic.gates.modcompat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.simplelogic.gates.SimpleLogicGates;

@CharsetJEIPlugin("simplelogic.gates")
public class JEIPluginGates implements IModPlugin {
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		subtypeRegistry.registerSubtypeInterpreter(SimpleLogicGates.itemGate, (stack) -> {
			String logicName = "dummy";
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("logic", Constants.NBT.TAG_STRING)) {
				logicName = stack.getTagCompound().getString("logic");
			}
			if (SimpleLogicGates.inversionSensitiveLogics.contains(logicName)) {
				return logicName + ";" + stack.getTagCompound().getByte("li");
			} else {
				return logicName;
			}
		});
	}
}
