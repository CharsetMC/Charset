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

package pl.asie.charset.lib.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;

public abstract class ModelColorHandler<T extends IRenderComparable<T>> implements IBlockColor, IItemColor {
    private final ModelFactory<T> parent;

    public ModelColorHandler(ModelFactory<T> parent) {
        this.parent = parent;
    }

    public abstract int colorMultiplier(T info, int tintIndex);

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (state instanceof IExtendedBlockState) {
            T info = ((IExtendedBlockState) state).getValue(parent.getProperty());
            if (info != null) {
                return colorMultiplier(info, tintIndex);
            }
        }
        return -1;
    }

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        T info = parent.fromItemStack(stack);
        if (info != null) {
            return colorMultiplier(info, tintIndex);
        }
        return -1;
    }
}
