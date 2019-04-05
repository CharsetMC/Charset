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

package pl.asie.simplelogic.gates.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.modcompat.mcmultipart.IMultipartBase;
import pl.asie.charset.lib.modcompat.mcmultipart.MultipartBase;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;

import java.util.Collections;
import java.util.List;

public class MultipartGate extends MultipartBase {
	@Override
	public Block getBlock() {
		return SimpleLogicGates.blockGate;
	}

	@Override
	public List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
		IPartSlot slot = part.getSlot();
		if (slot instanceof EnumFaceSlot) {
			return Collections.singletonList(PartGate.BOXES[((EnumFaceSlot) slot).getFacing().ordinal()]);
		} else {
			return Collections.singletonList(Block.FULL_BLOCK_AABB);
		}
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return EnumFaceSlot.fromFace(facing.getOpposite());
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof PartGate) {
			return EnumFaceSlot.fromFace(((PartGate) tile).getOrientation().facing.getOpposite());
		} else {
			return EnumCenterSlot.CENTER;
		}
	}
}
