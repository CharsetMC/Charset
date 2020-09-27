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

package pl.asie.charset.module.power.steam.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.steam.TileMirror;

import javax.annotation.Nullable;

public class MirrorColorHandler implements IBlockColor, IItemColor {
    public static final MirrorColorHandler INSTANCE = new MirrorColorHandler();

    private MirrorColorHandler() {

    }

    private int colorMultiplier(@Nullable ItemMaterial material) {
        if (material != null) {
            if (!material.getTypes().contains("block")) {
                ItemMaterial materialBlock = material.getRelated("block");
                if (materialBlock != null) {
                    material = materialBlock;
                }
            }

            return 0xFF000000 | ColorLookupHandler.INSTANCE.getColor(material.getStack(), RenderUtils.AveragingMode.FULL);
        } else {
            return 0xFFFFFFFF;
        }
    }

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 0) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileMirror) {
                return colorMultiplier(((TileMirror) tile).getMaterial());
            } else {
                return colorMultiplier(null);
            }
        } else {
            return -1;
        }
    }

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return colorMultiplier(ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material"));
        } else {
            return -1;
        }
    }
}
