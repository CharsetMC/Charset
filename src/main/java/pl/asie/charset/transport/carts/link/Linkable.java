package pl.asie.charset.transport.carts.link;

import com.google.common.base.Suppliers;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.utils.FunctionalUtils;
import pl.asie.charset.transport.carts.CharsetTransportCarts;
import pl.asie.charset.transport.carts.ModCharsetCarts;

import java.util.UUID;
import java.util.function.Supplier;

public final class Linkable {
	public static final Storage STORAGE = new Storage();
	public static final Supplier<CapabilityProviderFactory<Linkable>> PROVIDER = FunctionalUtils.lazySupplier(() -> new CapabilityProviderFactory<Linkable>(
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
