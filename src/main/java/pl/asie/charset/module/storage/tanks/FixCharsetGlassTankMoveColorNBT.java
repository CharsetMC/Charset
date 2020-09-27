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

package pl.asie.charset.module.storage.tanks;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.module.storage.barrels.ItemDayBarrel;
import pl.asie.charset.module.storage.tanks.ItemBlockTank;

public class FixCharsetGlassTankMoveColorNBT implements IFixableData {
    @Override
    public int getFixVersion() {
        return 4;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        if (compound.hasKey("id", 8) && compound.hasKey("")) {
            Item item = Item.getByNameOrId(compound.getString("id"));
            if (item instanceof ItemBlockTank) {
                int dmg = compound.getShort("Damage");
                if (dmg >= 1 && dmg <= 16) {
                    compound.setShort("Damage", (short) 0);
                    if (!compound.hasKey("tag", 10)) {
                        compound.setTag("tag", new NBTTagCompound());
                    }
                    compound.getCompoundTag("tag").setInteger("color", dmg - 1);
                }
            }
        }

        return compound;
    }
}
