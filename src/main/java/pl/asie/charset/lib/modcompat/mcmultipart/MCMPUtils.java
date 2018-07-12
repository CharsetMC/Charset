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
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class MCMPUtils {
	private MCMPUtils() {

	}

	private static void addSlot(IPartSlot slot, IMultipartContainer container, Collection<IPartSlot> partSlots, Stream.Builder<IPartInfo> builder) {
		if (slot != null) {
			partSlots.add(slot);
			container.get(slot).ifPresent(builder);
		}
	}

	// FIXME: Hack to work around deficiencies in MCMultiPart 2 as-is
	public static Stream<IPartInfo> streamParts(IMultipartContainer container, EnumFacing edge, EnumFacing face) {
		Stream.Builder<IPartInfo> streamBuilder = Stream.builder();
		Set<IPartSlot> partSlots = new HashSet<>();

		if (edge != null) addSlot(EnumEdgeSlot.fromFaces(edge, face), container, partSlots, streamBuilder);
		addSlot(EnumFaceSlot.fromFace(face), container, partSlots, streamBuilder);
		if (edge != null) addSlot(EnumFaceSlot.fromFace(edge), container, partSlots, streamBuilder);

		for (IPartSlot slot : container.getParts().keySet()) {
			if (partSlots.add(slot)) {
				addSlot(slot, container, partSlots, streamBuilder);
			}
		}

		return streamBuilder.build();
	}

	public static boolean placePartAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
	                                  float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state) {
		IPartSlot slot = multipartBlock.getSlotForPlacement(world, pos, state, facing, hitX, hitY, hitZ, player);
		if (!multipartBlock.canPlacePartAt(world, pos) || !multipartBlock.canPlacePartOnSide(world, pos, facing, slot))
			return false;

		if (MultipartHelper.addPart(world, pos, slot, state, false)) {
			if (!world.isRemote) {
				IPartInfo info = MultipartHelper.getContainer(world, pos).flatMap(c -> c.get(slot)).orElse(null);
				if (info != null) {
					ItemBlockMultipart.setMultipartTileNBT(player, stack, info);
					if (multipartBlock instanceof IMultipartBase) {
						((IMultipartBase) multipartBlock).onPartPlacedBy(info, player, stack, facing, hitX, hitY, hitZ);
					} else {
						multipartBlock.onPartPlacedBy(info, player, stack);
					}
				}
			}
			return true;
		}
		return false;
	}
}
