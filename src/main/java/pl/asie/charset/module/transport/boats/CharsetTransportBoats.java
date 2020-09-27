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

package pl.asie.charset.module.transport.boats;

import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.storage.barrels.EntityMinecartDayBarrel;

/* @CharsetModule(
		name = "transport.boats",
		description = "Boats out of any material!",
		profile = ModuleProfile.EXPERIMENTAL
) */
public class CharsetTransportBoats {
	public static ItemBoatCharset itemBoat;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(EntityBoatCharset.class, "boatCharset", 80, 3, true);
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), (itemBoat = new ItemBoatCharset()), "boat");
	}

	@SubscribeEvent
	public void onJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity().getClass() == EntityBoat.class) {
			ItemStack boatStack = ItemBoatCharset.STACK.get();
			ItemMaterial plankMaterial = (boatStack != null)
					? ItemBoatCharset.getMaterial(boatStack)
					: ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.PLANKS, 1, ((EntityBoat) event.getEntity()).getBoatType().getMetadata()));
			event.setCanceled(true);

			EntityBoatCharset entityBoatCharset = new EntityBoatCharset(event.getWorld(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, plankMaterial);
			entityBoatCharset.rotationYaw = event.getEntity().rotationYaw;
			event.getWorld().spawnEntity(entityBoatCharset);
		}
	}
}
