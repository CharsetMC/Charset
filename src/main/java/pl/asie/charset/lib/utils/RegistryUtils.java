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

package pl.asie.charset.lib.utils;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.ModCharset;

import java.util.Map;

public final class RegistryUtils {
	private static final TIntObjectMap<String> takenEntityIds = new TIntObjectHashMap<>();

	private RegistryUtils() {

	}

	public static void registerModel(Item item, int meta, String name) {
		UtilProxyCommon.proxy.registerItemModel(item, meta, name);
	}

	public static void register(Class<? extends TileEntity> tileEntity, String name) {
		GameRegistry.registerTileEntity(tileEntity, new ResourceLocation("charset", name).toString());
	}

	public static void register(Class<? extends Entity> entity, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		int id;

		ConfigCategory category = ModCharset.configIds.getCategory("entity");
		if (category.containsKey(name)) {
			Property prop = category.get(name);
			id = prop.getInt();
		} else {
			id = 1;
			while (takenEntityIds.containsKey(id)) id++;
			ModCharset.configIds.get("entity", name, id);
		}

		ResourceLocation nameLoc = new ResourceLocation("charset", name);
		EntityRegistry.registerModEntity(nameLoc, entity, nameLoc.toString(), id, ModCharset.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
		takenEntityIds.put(id, name);
	}

	public static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistry<T> registry, T object, String name) {
		register(registry, object, name, ModCharset.CREATIVE_TAB);
	}

	public static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistry<T> registry, T object, String name, CreativeTabs tab) {
		if (object == null) {
			ModCharset.logger.error("Trying to register null object " + name + "! This usually signifies a worse crash in Charset.");
			return;
		}

		if (object.getRegistryName() == null) {
			if (name != null) {
				object.setRegistryName(new ResourceLocation(ModCharset.MODID, name));
			} else {
				throw new RuntimeException("Unknown name for object!");
			}
		}

		registry.register(object);
		UtilProxyCommon.proxy.setTabAndNameIfNotPresent(object, name, tab);
	}

	public static void loadConfigIds(Configuration configIds) {
		ConfigCategory cat = configIds.getCategory("entity");
		for (Map.Entry<String, Property> e : cat.entrySet()) {
			if (e.getValue().isIntValue()) {
				takenEntityIds.put(e.getValue().getInt(), e.getKey());
			}
		}
	}
}
