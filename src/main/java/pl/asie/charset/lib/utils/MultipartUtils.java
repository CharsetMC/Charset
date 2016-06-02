/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.utils;

import java.util.Iterator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;

public final class MultipartUtils {
	private MultipartUtils() {

	}

	public static boolean hasCapability(Capability cap, World world, BlockPos pos, PartSlot slot, EnumFacing side) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container == null) {
			TileEntity tile = world.getTileEntity(pos);
			return tile != null ? tile.hasCapability(cap, side) : false;
		} else {
			return container.hasCapability(cap, slot, side);
		}
	}

	public static <T> T getCapability(Capability<T> cap, World world, BlockPos pos, PartSlot slot, EnumFacing side) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container == null) {
			TileEntity tile = world.getTileEntity(pos);
			return tile != null ? tile.getCapability(cap, side) : null;
		} else {
			return container.getCapability(cap, slot, side);
		}
	}

	public static <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container == null) {
			TileEntity tile = world.getTileEntity(pos);
			return tile != null && clazz.isAssignableFrom(tile.getClass()) ? (T) tile : null;
		} else {
			if (side == null) {
				return getInterfaceCenter(clazz, container);
			}

			EnumFacing[] var2 = EnumFacing.VALUES;
			int var3 = var2.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				EnumFacing face = var2[var4];
				if (face != side && face != side.getOpposite()) {
					T tmp = getInterface(clazz, container, side, face);
					if (tmp != null) {
						return tmp;
					}
				}
			}

			return null;
		}
	}

	public static <T> T getInterfaceCenter(Class<T> clazz, IMultipartContainer container) {
		ISlottedPart part = container.getPartInSlot(PartSlot.CENTER);
		if (part != null && clazz.isAssignableFrom(part.getClass())) {
			return (T) part;
		} else {
			Iterator var4 = container.getParts().iterator();

			IMultipart p;
			do {
				do {
					if (!var4.hasNext()) {
						return null;
					}

					p = (IMultipart) var4.next();
				} while (p instanceof ISlottedPart && !((ISlottedPart) p).getSlotMask().isEmpty());
			} while (p == null || !clazz.isAssignableFrom(p.getClass()));

			return (T) p;
		}
	}

	public static <T> T getInterface(Class<T> clazz, IMultipartContainer container, EnumFacing side, EnumFacing face) {
		if (container == null) {
			return null;
		} else if (side == null) {
			return getInterfaceCenter(clazz, container);
		} else if (face == side.getOpposite()) {
			return null;
		}

		ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(side));
		if (part != null) {
			return clazz.isAssignableFrom(part.getClass()) ? (T) part : null;
		} else if (side != face && face != null) {
			part = container.getPartInSlot(PartSlot.getEdgeSlot(side, face));
			if (part != null) {
				return clazz.isAssignableFrom(part.getClass()) ? (T) part : null;
			} else {
				part = container.getPartInSlot(PartSlot.getFaceSlot(face));
				if (part != null) {
					return clazz.isAssignableFrom(part.getClass()) ? (T) part : null;
				} else {
					return getInterfaceCenter(clazz, container);
				}
			}
		} else {
			return getInterfaceCenter(clazz, container);
		}
	}

	public static <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side, EnumFacing face) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container == null) {
			TileEntity tile = world.getTileEntity(pos);
			return tile != null && clazz.isAssignableFrom(tile.getClass()) ? (T) tile : null;
		} else {
			if (side == null) {
				return getInterfaceCenter(clazz, container);
			}

			return getInterface(clazz, container, side, face);
		}
	}
}
