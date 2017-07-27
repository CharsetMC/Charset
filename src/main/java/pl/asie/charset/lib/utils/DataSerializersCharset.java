package pl.asie.charset.lib.utils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

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

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            DataSerializers.registerSerializer(OUATERNION);
            initialized = true;
        }
    }
}
