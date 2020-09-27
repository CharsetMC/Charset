/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tweak.carry.compat.railcraft;

import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.tweak.carry.CarryTransformerEntityMinecart;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerEntityMinecartRailcraft extends CarryTransformerEntityMinecart {
    private static class ClassNames {
        public static final String RAILCRAFT_CHEST = "mods.railcraft.common.carts.EntityCartChest";
        public static final String RAILCRAFT_CHEST_METALS = "mods.railcraft.common.carts.EntityCartChestMetals";
        public static final String RAILCRAFT_JUKEBOX = "mods.railcraft.common.carts.EntityCartJukebox";
    }

    @Override
    protected Pair<IBlockState, TileEntity> getExtractedPair(@Nonnull Entity object, boolean simulate) {
        String className = object.getClass().getName();
        if (ClassNames.RAILCRAFT_CHEST.equals(className)) {
            TileEntityChest tile = new TileEntityChest();
            copyEntityToTile(tile, object, "Items");
            return Pair.of(Blocks.CHEST.getDefaultState(), tile);
        } else if (ClassNames.RAILCRAFT_JUKEBOX.equals(className)) {
            BlockJukebox.TileEntityJukebox tile = new BlockJukebox.TileEntityJukebox();
            copyEntityToTile(tile, object, "RecordItem", "Record");
            return Pair.of(Blocks.JUKEBOX.getDefaultState(), tile);
        } else {
            return null;
        }
    }

    @Override
    public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
        if (state.getBlock() == Blocks.CHEST) {
            if (tile != null && transform(object, ClassNames.RAILCRAFT_CHEST,
                    filter(tile.writeToNBT(new NBTTagCompound()), "Items"),
                    simulate) != null) {
                return true;
            }
        } else if (state.getBlock() == Blocks.JUKEBOX) {
            if (tile != null && transform(object, ClassNames.RAILCRAFT_JUKEBOX,
                    filter(tile.writeToNBT(new NBTTagCompound()), "Items"),
                    simulate) != null) {
                return true;
            }
        }
        return false;
    }
}
