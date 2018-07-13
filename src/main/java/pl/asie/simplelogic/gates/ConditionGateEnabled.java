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

package pl.asie.simplelogic.gates;

import net.minecraft.util.ResourceLocation;
import pl.asie.charset.module.storage.barrels.BarrelUpgrade;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;

import java.util.function.BooleanSupplier;

public class ConditionGateEnabled implements BooleanSupplier {
	private final ResourceLocation gate;

	public ConditionGateEnabled(ResourceLocation gate) {
		this.gate = gate;
	}

	@Override
	public boolean getAsBoolean() {
		return SimpleLogicGates.logicClasses.containsKey(gate);
	}
}
