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

package pl.asie.charset.lib.modcompat.commoncapabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
	name = "commoncapabilities:lib.slotlessInsertion",
	profile = ModuleProfile.COMPAT,
	dependencies = {"mod:commoncapabilities"}
)
public class CapabilityWrapperSlotlessInsertion implements CapabilityHelper.Wrapper<IItemInsertionHandler> {
	@CapabilityInject(ISlotlessItemHandler.class)
	public static Capability<ISlotlessItemHandler> CAP;
	@CharsetModule.Instance
	public static CapabilityWrapperSlotlessInsertion instance;

	@Mod.EventHandler
	public void register(FMLPostInitializationEvent event) {
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, instance);
	}

	@Override
	public IItemInsertionHandler get(ICapabilityProvider provider, EnumFacing side) {
		ISlotlessItemHandler handler = CapabilityHelper.get(CAP, provider, side);
		if (handler != null) {
			return new IItemInsertionHandler() {
				@Override
				public ItemStack insertItem(ItemStack stack, boolean simulate) {
					return handler.insertItem(stack, simulate);
				}
			};
		} else {
			return null;
		}
	}
}
