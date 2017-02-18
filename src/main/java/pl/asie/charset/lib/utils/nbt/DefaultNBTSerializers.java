package pl.asie.charset.lib.utils.nbt;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagShort;
import net.minecraftforge.common.util.Constants;

public final class DefaultNBTSerializers {
    private DefaultNBTSerializers() {

    }

    public static void init() {
        NBTSerializer.INSTANCE.register(Byte.class, Constants.NBT.TAG_BYTE, NBTTagByte::new, (tag) -> ((NBTTagByte) tag).getByte());
        NBTSerializer.INSTANCE.register(Short.class, Constants.NBT.TAG_SHORT, NBTTagShort::new, (tag) -> ((NBTTagShort) tag).getShort());
    }
}
