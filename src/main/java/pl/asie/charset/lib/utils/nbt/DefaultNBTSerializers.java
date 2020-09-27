/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
