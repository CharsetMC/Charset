package pl.asie.charset.module.power.steam;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class SteamChunkContainerStorage implements Capability.IStorage<SteamChunkContainer> {
	@Nullable
	@Override
	public NBTBase writeNBT(Capability<SteamChunkContainer> capability, SteamChunkContainer instance, EnumFacing side) {
		NBTTagCompound cpd = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (SteamParticle particle : instance.getParticles()) {
			list.appendTag(particle.serializeNBT());
		}
		cpd.setTag("particles", list);
		return cpd;
	}

	@Override
	public void readNBT(Capability<SteamChunkContainer> capability, SteamChunkContainer instance, EnumFacing side, NBTBase nbt) {
		if (instance.getChunk() != null && instance.getChunk().getWorld() != null && nbt instanceof NBTTagCompound) {
			NBTTagCompound cpd = (NBTTagCompound) nbt;
			if (cpd.hasKey("particles", Constants.NBT.TAG_LIST)) {
				NBTTagList list = cpd.getTagList("particles", Constants.NBT.TAG_COMPOUND);
				instance.getParticles().clear();
				for (int i = 0; i < list.tagCount(); i++) {
					SteamParticle p = new SteamParticle(instance.getChunk().getWorld());
					p.deserializeNBT(list.getCompoundTagAt(i));
					instance.getParticles().add(p);
				}
			}
		}
	}
}
