package pl.asie.charset.lib.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderFactory<T> {
	private final Capability<T> capability;
	private final Capability.IStorage<T> storage;

	private class Provider implements ICapabilitySerializable<NBTBase> {
		private final T object;

		private Provider(T object) {
			this.object = object;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
			return capability == CapabilityProviderFactory.this.capability;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
			return capability == CapabilityProviderFactory.this.capability ? CapabilityProviderFactory.this.capability.cast(object) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return storage.writeNBT(capability, object, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			storage.readNBT(capability, object, null, nbt);
		}
	}

	public CapabilityProviderFactory(Capability<T> capability, Capability.IStorage<T> storage) {
		this.capability = capability;
		this.storage = storage;
	}

	public ICapabilityProvider create(T object) {
		return new Provider(object);
	}

	public Capability.IStorage<T> getStorage() {
		return storage;
	}
}
