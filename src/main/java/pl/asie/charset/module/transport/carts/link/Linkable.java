/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.transport.carts.link;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.utils.FunctionalUtils;
import pl.asie.charset.module.transport.carts.CharsetTransportCarts;

import java.util.UUID;
import java.util.function.Supplier;

public final class Linkable {
	public static final Storage STORAGE = new Storage();
	public static final Supplier<CapabilityProviderFactory<Linkable>> PROVIDER = FunctionalUtils.lazySupplier(() -> new CapabilityProviderFactory<>(
			CharsetTransportCarts.LINKABLE, STORAGE
	));
	public static final ResourceLocation ID = new ResourceLocation("charsetcarts", "linkable");
	public Linkable previous, next;
	private final Entity owner;

	public Linkable(Entity owner) {
		this.owner = owner;
	}

	public Entity getOwner() {
		return owner;
	}

	public UUID getId() {
		return owner.getPersistentID();
	}

	public static class Storage implements Capability.IStorage<Linkable> {
		@Override
		public NBTBase writeNBT(Capability<Linkable> capability, Linkable instance, EnumFacing side) {
			NBTTagCompound compound = new NBTTagCompound();
			if (instance.previous != null)
				compound.setUniqueId("prev", instance.previous.getId());
			if (instance.next != null)
				compound.setUniqueId("next", instance.next.getId());
			return compound;
		}

		@Override
		public void readNBT(Capability<Linkable> capability, Linkable instance, EnumFacing side, NBTBase nbt) {
			instance.previous = null;
			instance.next = null;

			if (nbt instanceof NBTTagCompound) {
				NBTTagCompound compound = (NBTTagCompound) nbt;
				if (compound.hasKey("prev")) {
					Linkable prev = CharsetTransportCarts.linker.get(compound.getUniqueId("prev"));
					if (prev != null) {
						CharsetTransportCarts.linker.link(prev, instance);
					}
				}

				if (compound.hasKey("next")) {
					Linkable next = CharsetTransportCarts.linker.get(compound.getUniqueId("next"));
					if (next != null) {
						CharsetTransportCarts.linker.link(instance, next);
					}
				}
			}
		}
	}
}
