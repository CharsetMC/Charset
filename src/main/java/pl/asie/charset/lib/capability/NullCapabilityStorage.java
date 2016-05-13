package pl.asie.charset.lib.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Created by asie on 5/13/16.
 */
public class NullCapabilityStorage<T> implements Capability.IStorage<T> {
    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
        return null;
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {

    }
}
