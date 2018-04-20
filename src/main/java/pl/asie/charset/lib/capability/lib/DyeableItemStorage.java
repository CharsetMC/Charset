package pl.asie.charset.lib.capability.lib;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.lib.IDyeableItem;

import javax.annotation.Nullable;

public class DyeableItemStorage implements Capability.IStorage<IDyeableItem> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IDyeableItem> capability, IDyeableItem instance, EnumFacing side) {
        NBTTagCompound colors = new NBTTagCompound();
        for (int i = 0; i < instance.getColorSlotCount(); i++) {
            if (instance.hasColor(i)) {
                colors.setInteger("color" + i, instance.getColor(i));
            }
        }
        return colors;
    }

    @Override
    public void readNBT(Capability<IDyeableItem> capability, IDyeableItem instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound colors = (NBTTagCompound) nbt;
            for (int i = 0; i < instance.getColorSlotCount(); i++) {
                String key = "color" + i;
                if (colors.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC)) {
                    instance.setColor(i, colors.getInteger(key));
                } else {
                    instance.removeColor(i);
                }
            }
        }
    }
}
