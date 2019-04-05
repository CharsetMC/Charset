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

package pl.asie.charset.module.tweak.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.tweak.carry.CarryHandler;
import pl.asie.charset.module.tweak.carry.CharsetTweakBlockCarrying;
import pl.asie.charset.module.tweak.carry.ICarryTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerPlayerShare implements ICarryTransformer<Entity> {
    @Nullable
    @Override
    public Pair<IBlockState, TileEntity> extract(@Nonnull Entity object, boolean simulate) {
        return null;
    }

    @Override
    public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
        if (object instanceof EntityPlayer && object.hasCapability(CharsetTweakBlockCarrying.CAPABILITY, null)) {
            EntityPlayer player = (EntityPlayer) object;
            CarryHandler handler = object.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
            if (handler == null || handler.isCarrying() || !CharsetTweakBlockCarrying.canPlayerConsiderCarryingBlock(player)) {
                return false;
            }

            if (!simulate) {
                handler.put(state, tile);
                CharsetTweakBlockCarrying.syncCarryWithAllClients(player);
            }
            return true;
        }

        return false;
    }
}
