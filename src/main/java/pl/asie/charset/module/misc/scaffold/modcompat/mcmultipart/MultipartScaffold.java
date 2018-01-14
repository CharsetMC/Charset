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

package pl.asie.charset.module.misc.scaffold.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MultipartScaffold implements IMultipart {
    public static class Slot implements IPartSlot {
        public static final Slot INSTANCE = new Slot();

        @Override
        public EnumSlotAccess getFaceAccess(EnumFacing face) {
            return EnumSlotAccess.MERGE;
        }

        @Override
        public int getFaceAccessPriority(EnumFacing face) {
            return 250;
        }

        @Override
        public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
            return EnumSlotAccess.NONE;
        }

        @Override
        public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
            return 200;
        }

        @Nullable
        @Override
        public ResourceLocation getRegistryName() {
            return new ResourceLocation("charset:slot_scaffold");
        }
    }

    private static final AxisAlignedBB OCCLUSION_BOX_TOP = new AxisAlignedBB(0, 1 - 0.0625, 0, 1, 1, 1);

    @Override
    public List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
        return Collections.singletonList(OCCLUSION_BOX_TOP);
    }

    @Override
    public Block getBlock() {
        return CharsetMiscScaffold.scaffoldBlock;
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        return Slot.INSTANCE;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return Slot.INSTANCE;
    }
}
