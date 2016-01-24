package pl.asie.charset.lib.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import pl.asie.charset.api.wires.IBundledEmitter;

public class DefaultBundledEmitterStorage implements Capability.IStorage<IBundledEmitter> {
	@Override
	public NBTBase writeNBT(Capability<IBundledEmitter> capability, IBundledEmitter instance, EnumFacing side) {
		if (instance instanceof DefaultBundledEmitter) {
			NBTTagCompound cpd = new NBTTagCompound();
			cpd.setByteArray("s", instance.getBundledSignal());
			return cpd;
		}
		return null;
	}

	@Override
	public void readNBT(Capability<IBundledEmitter> capability, IBundledEmitter instance, EnumFacing side, NBTBase nbt) {
		if (instance instanceof DefaultBundledEmitter && nbt instanceof NBTTagCompound) {
			NBTTagCompound cpd = (NBTTagCompound) nbt;
			if (cpd.hasKey("s")) {
				byte[] data = cpd.getByteArray("s");
				if (data != null && data.length == 16) {
					((DefaultBundledEmitter) instance).emit(data);
				}
			}
		}
	}
}
