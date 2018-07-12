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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.MultipartCapabilityHelper;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.multipart.MultipartRedstoneHelper;
import mcmultipart.api.slot.EnumEdgeSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.utils.redstone.IRedstoneGetter;

import javax.annotation.Nullable;
import java.util.Optional;

public class RedstoneGetterMultipart implements IRedstoneGetter {
	@Override
	public int get(IBlockAccess world, BlockPos pos, EnumFacing face, @Nullable EnumFacing edge) {
		Optional<IMultipartContainer> container = MultipartHelper.getContainer(world, pos);
		//noinspection OptionalIsPresent
		if (container.isPresent()) {
			return MCMPUtils.streamParts(container.get(), edge, face.getOpposite()).map(
					(info) -> {
						if (info.getTile().hasPartCapability(Capabilities.REDSTONE_EMITTER, face.getOpposite())) {
							return info.getTile().getPartCapability(Capabilities.REDSTONE_EMITTER, face.getOpposite()).getRedstoneSignal();
						} else if (info.getState().canProvidePower()) {
							return info.getPart().getWeakPower(info.getPartWorld(), info.getPartPos(), info, face);
						} else {
							return -1;
						}
					}
			).filter((a) -> a >= 0).findFirst().orElse(0);
		} else {
			return -1;
		}
	}
}
