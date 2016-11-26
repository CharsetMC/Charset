/*
 * Copyright (c) 2015-2016 Adrian Siekierka, rubensworks
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

package pl.asie.charset.pipes.shifter;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.pipes.IShifter;

/**
 * Default storage implementation for the shifter capability.
 * @author rubensworks
 */
public class ShifterStorage implements Capability.IStorage<IShifter> {
    @Override
    public NBTBase writeNBT(Capability<IShifter> capability, IShifter instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("isExtract", instance.getMode() == IShifter.Mode.Extract);
        tag.setInteger("direction", instance.getDirection().ordinal());
        tag.setInteger("shiftDistance", instance.getShiftDistance());
        tag.setBoolean("isShifting", instance.isShifting());
        tag.setBoolean("hasFilter", instance.hasFilter());
        ItemStack filter = ((ShifterImpl) instance).getFilter();
        if (!filter.isEmpty()) {
            tag.setTag("filter", filter.serializeNBT());
        }
        return tag;
    }

    @Override
    public void readNBT(Capability<IShifter> capability, IShifter instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        ShifterImpl shifter = (ShifterImpl) instance;
        shifter.setMode(tag.getBoolean("isExtract") ? IShifter.Mode.Extract : IShifter.Mode.Shift);
        shifter.setDirection(EnumFacing.VALUES[tag.getInteger("direction")]);
        shifter.setShiftDistance(tag.getInteger("shiftDistance"));
        shifter.setShifting(tag.getBoolean("isShifting"));
        shifter.setHasFilter(tag.getBoolean("hasFilter"));
        if(tag.hasKey("filter", Constants.NBT.TAG_COMPOUND)) {
            shifter.setFilter(new ItemStack(tag.getCompoundTag("filter")));
        }
    }
}
