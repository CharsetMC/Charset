package pl.asie.charset.lib.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public final class DataSerializersCharset {
    private DataSerializersCharset() {

    }

    public static final DataSerializer<Quaternion> OUATERNION = new DataSerializer<Quaternion>() {
        @Override
        public void write(PacketBuffer buf, Quaternion value) {
            buf.writeFloat((float) value.w);
            buf.writeFloat((float) value.x);
            buf.writeFloat((float) value.y);
            buf.writeFloat((float) value.z);
        }

        @Override
        public Quaternion read(PacketBuffer buf) {
            double w = buf.readFloat();
            double x = buf.readFloat();
            double y = buf.readFloat();
            double z = buf.readFloat();
            return new Quaternion(w, x, y, z);
        }

        @Override
        public DataParameter<Quaternion> createKey(int id)
        {
            return new DataParameter(id, this);
        }

        @Override
        public Quaternion copyValue(Quaternion value) {
            return new Quaternion(value);
        }
    };

    public static final DataSerializer<NBTTagCompound> NBT_TAG_COMPOUND = new DataSerializer<NBTTagCompound>() {
        @Override
        public void write(PacketBuffer buf, NBTTagCompound value) {
            ByteBufUtils.writeTag(buf, value);
        }

        @Override
        public NBTTagCompound read(PacketBuffer buf) {
            return ByteBufUtils.readTag(buf);
        }

        @Override
        public DataParameter<NBTTagCompound> createKey(int id) {
            return new DataParameter(id, this);
        }

        @Override
        public NBTTagCompound copyValue(NBTTagCompound value) {
            return value.copy();
        }
    };

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            DataSerializers.registerSerializer(OUATERNION);
            DataSerializers.registerSerializer(NBT_TAG_COMPOUND);
            initialized = true;
        }
    }
}
