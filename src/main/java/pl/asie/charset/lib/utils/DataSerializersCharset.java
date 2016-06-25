package pl.asie.charset.lib.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.factorization.Quaternion;

import java.io.IOException;

public class DataSerializersCharset {
    public static final DataSerializer<Quaternion> FZ_OUATERNION = new DataSerializer<Quaternion>() {
        public void write(PacketBuffer buf, Quaternion value) {
            buf.writeFloat((float) value.w);
            buf.writeFloat((float) value.x);
            buf.writeFloat((float) value.y);
            buf.writeFloat((float) value.z);
        }
        public Quaternion read(PacketBuffer buf) {
            double w = buf.readFloat();
            double x = buf.readFloat();
            double y = buf.readFloat();
            double z = buf.readFloat();
            return new Quaternion(w, x, y, z);
        }
        public DataParameter<Quaternion> createKey(int id)
        {
            return new DataParameter(id, this);
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
    };

    static {
        DataSerializers.registerSerializer(FZ_OUATERNION);
        DataSerializers.registerSerializer(NBT_TAG_COMPOUND);
    }
}
