/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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
