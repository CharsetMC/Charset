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

package pl.asie.charset.lib.modcompat.baubles;

import baubles.api.cap.IBaublesItemHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.api.lib.EntityGatherItemsEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
	name = "baubles:lib",
	dependencies = {"mod:baubles"},
	profile = ModuleProfile.COMPAT
)
public class CharsetBaublesCompat {
	@CapabilityInject(IBaublesItemHandler.class)
	public static Capability baublesItemHandler;
	@CharsetModule.Instance
	public static CharsetBaublesCompat instance;

	@Mod.EventHandler
	public void register(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onGatherItems(EntityGatherItemsEvent event) {
		if (event.collectsWorn()) {
			if (baublesItemHandler != null && event.getEntity().hasCapability(baublesItemHandler, null)) {
				IItemHandler handler = (IItemHandler) event.getEntity().getCapability(baublesItemHandler, null);
				for (int i = 0; i < handler.getSlots(); i++) {
					event.addStack(handler.getStackInSlot(i));
				}
			}
		}
	}
}
