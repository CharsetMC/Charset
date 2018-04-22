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

package pl.asie.charset.lib.notify.component;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.network.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public class NotificationComponentUtil {
	private static final TIntObjectMap<NotificationComponentFactory> factoriesById;
	private static final TObjectIntMap<Class> factoryIdsByClass;
	private static final Map<Class, NotificationComponentFactory> factoriesByClass;

	static {
		factoriesById = new TIntObjectHashMap<>();
		factoryIdsByClass = new TObjectIntHashMap<>();
		factoriesByClass = new HashMap<>();

		register(0x01, new NotificationComponentString.Factory());
		register(0x02, new NotificationComponentTextComponent.Factory());

		register(0x10, new NotificationComponentItemStack.Factory());
		register(0x11, new NotificationComponentFluidStack.Factory());
	}

	public static boolean register(int id, NotificationComponentFactory factory) {
		if (factoriesById.containsKey(id)) {
			throw new RuntimeException("NotificationComponentFactory with ID " + id + " already exists!");
		}

		factoriesById.put(id, factory);
		factoriesByClass.put(factory.getComponentClass(), factory);
		factoryIdsByClass.put(factory.getComponentClass(), id);
		return true;
	}

	public static NotificationComponent deserialize(PacketBuffer buf) {
		int id = buf.readVarInt();
		if (id != Integer.MIN_VALUE && factoriesById.containsKey(id)) {
			return factoriesById.get(id).deserialize(buf);
		} else {
			return NotificationComponentUnknown.INSTANCE;
		}
	}

	public static void serialize(NotificationComponent component, PacketBuffer buf) {
		NotificationComponentFactory factory = factoriesByClass.get(component.getClass());
		if (factory != null) {
			buf.writeVarInt(factoryIdsByClass.get(component.getClass()));
			factory.serialize(component, buf);
		} else {
			buf.writeVarInt(Integer.MIN_VALUE);
		}
	}
}
