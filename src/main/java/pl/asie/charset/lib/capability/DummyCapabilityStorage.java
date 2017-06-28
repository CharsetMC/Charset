/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class DummyCapabilityStorage<T> implements Capability.IStorage<T> {
    private static final DummyCapabilityStorage INSTANCE = new DummyCapabilityStorage();

    private DummyCapabilityStorage() {

    }

    @SuppressWarnings("unchecked")
    public static <T> DummyCapabilityStorage<T> get() {
        return (DummyCapabilityStorage<T>) INSTANCE;
    }

    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
        return new NBTTagCompound();
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {

    }
}
