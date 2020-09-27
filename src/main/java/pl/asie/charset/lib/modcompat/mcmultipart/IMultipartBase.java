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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.block.BlockBase;

public interface IMultipartBase extends IMultipart {
	default void onPartPlacedBy(IPartInfo part, EntityLivingBase placer, ItemStack stack, EnumFacing face, float hitX, float hitY, float hitZ) {
		Block b = part.getState().getBlock();
		if (b instanceof BlockBase) {
			((BlockBase) b).onBlockPlacedBy(part.getPartWorld(), part.getPartPos(), part.getState(), placer, stack, face, hitX, hitY, hitZ);
		} else {
			b.onBlockPlacedBy(part.getPartWorld(), part.getPartPos(), part.getState(), placer, stack);
		}
	}
}
