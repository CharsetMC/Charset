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

package pl.asie.charset.lib.modcompat.infraredstone;

import com.elytradev.infraredstone.api.IEncoderScannable;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import java.lang.reflect.Method;

@CharsetModule(
		name = "infraredstone:lib",
		profile = ModuleProfile.COMPAT,
		dependencies = {"mod:infraredstone"}
)
public class CharsetCompatInfraRedstone {
	private static final ResourceLocation LOC = new ResourceLocation("charset:infraredstone.encoderscannable");

	@CapabilityInject(IEncoderScannable.class)
	public Capability<IEncoderScannable> cap;

	private CapabilityProviderFactory<IEncoderScannable> factory;
	private final Object2BooleanMap<Class> cache = new Object2BooleanOpenHashMap<>();

	public static class CapabilityImpl implements IEncoderScannable {
		private final TileBase tile;

		public CapabilityImpl(TileBase tile) {
			this.tile = tile;
		}

		@Override
		public int getComparatorValue() {
			return tile.getComparatorValue(63);
		}
	}

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<TileEntity> event) {
		if (cap != null && event.getObject() instanceof TileBase) {
			if (cache.computeIfAbsent(event.getClass(),
					(c) -> {
						Method m = MethodHandleHelper.reflectMethodRecurse(event.getClass(), true, "getComparatorValue", "getComparatorValue", int.class);
						return m != null && m.getDeclaringClass() != TileBase.class;
					})) {

				if (factory == null) {
					factory = new CapabilityProviderFactory<>(cap);
					event.addCapability(LOC, factory.create(new CapabilityImpl((TileBase) event.getObject())));
				}
			}
		}
	}
}
