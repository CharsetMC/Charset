package pl.asie.charset.lib.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import pl.asie.charset.api.wires.IRedstoneEmitter;

public class DefaultRedstoneEmitterStorage implements Capability.IStorage<IRedstoneEmitter> {
	@Override
	public NBTBase writeNBT(Capability<IRedstoneEmitter> capability, IRedstoneEmitter instance, EnumFacing side) {
		if (instance instanceof DefaultRedstoneEmitter) {
			NBTTagCompound cpd = new NBTTagCompound();
			cpd.setInteger("s", instance.getRedstoneSignal());
			return cpd;
		}
		return null;
	}

	@Override
	public void readNBT(Capability<IRedstoneEmitter> capability, IRedstoneEmitter instance, EnumFacing side, NBTBase nbt) {
		if (instance instanceof DefaultRedstoneEmitter && nbt instanceof NBTTagCompound) {
			NBTTagCompound cpd = (NBTTagCompound) nbt;
			if (cpd.hasKey("s")) {
				((DefaultRedstoneEmitter) instance).emit(cpd.getInteger("s"));
			}
		}
	}
}
