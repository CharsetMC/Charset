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

package pl.asie.charset.lib.modcompat.buildcraft;

import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.PipeApi;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.modcompat.opencomputers.DriverCapability;
import pl.asie.charset.lib.modcompat.opencomputers.ManagedEnvironmentBarrel;

@CharsetModule(
        name = "buildcraft:lib",
        profile = ModuleProfile.COMPAT,
        dependencies = {"mod:buildcraftlib"}
)
public class CharsetCompatBC {
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, (provider, side) -> {
			if (provider.hasCapability(PipeApi.CAP_INJECTABLE, side)) {
				IInjectable injectable = provider.getCapability(PipeApi.CAP_INJECTABLE, side);
				if (injectable != null && injectable.canInjectItems(side)) {
					return new ItemInsertionHandlerBuildCraft(injectable, side);
				}
			}

			return null;
		});
	}
}
