package pl.asie.charset.carts.link;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.carts.ModCharsetCarts;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;

import java.util.UUID;

public class Linkable {
	public static final CapabilityProviderFactory<Linkable> PROVIDER = new CapabilityProviderFactory<>(ModCharsetCarts.LINKABLE, new Storage());
	public static final ResourceLocation ID = new ResourceLocation("charsetcarts", "linkable");
	private UUID id;

	public Linkable() {

	}

	public UUID getId() {
		return id;
	}

	public boolean hasId() {
		return id != null;
	}

	protected void setId(UUID id) {
		this.id = id;
	}

	public static class Storage implements Capability.IStorage<Linkable> {
		@Override
		public NBTBase writeNBT(Capability<Linkable> capability, Linkable instance, EnumFacing side) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setUniqueId("id", instance.id);
			return compound;
		}

		@Override
		public void readNBT(Capability<Linkable> capability, Linkable instance, EnumFacing side, NBTBase nbt) {
			if (nbt instanceof NBTTagCompound) {
				instance.setId(((NBTTagCompound) nbt).getUniqueId("id"));
			} else {
				instance.setId(null);
			}
		}
	}
}
