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

package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.nbt.NBTTagCompound;

public class CharsetManagedEnvironment extends AbstractManagedEnvironment implements NamedBlock {
	private final String name;
	private final int priority;

	public CharsetManagedEnvironment(String name, Visibility visibility, int priority) {
		this.name = name;
		this.priority = priority;
		this.setNode(Network.newNode(this, visibility).withComponent(name, visibility).create());
	}

	@Override
	public String preferredName() {
		return name;
	}

	@Override
	public int priority() {
		return priority;
	}
}
