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

package pl.asie.charset.lib.modcompat.tis3d;

import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.serial.SerialProtocolDocumentationReference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SerialInterfaceTile<T extends TileEntity> implements SerialInterface {
	@FunctionalInterface
	public interface Verifier<T extends TileEntity> {
		boolean worksWith(T tile, EnumFacing enumFacing);
	}

	@FunctionalInterface
	public interface Supplier<T extends TileEntity> {
		SerialInterface apply(T tile, EnumFacing enumFacing);
	}

	public static class Provider<T extends TileEntity> implements SerialInterfaceProvider {
		private final Supplier<T> supplier;
		private final Class<T> cl;
		private final Verifier<T> verifier;

		public Provider(Class<T> cl, Supplier<T> supplier) {
			this(cl, supplier, (a, b) -> true);
		}

		public Provider(Class<T> cl, Supplier<T> supplier, Verifier<T> verifier) {
			this.cl = cl;
			this.supplier = supplier;
			this.verifier = verifier;
		}

		@Override
		public boolean worksWith(World world, BlockPos blockPos, EnumFacing enumFacing) {
			TileEntity tile = world.getTileEntity(blockPos);
			if (cl.isInstance(tile)) {
				return verifier.worksWith((T) tile, enumFacing);
			} else {
				return false;
			}
		}

		@Nullable
		@Override
		public SerialInterface interfaceFor(World world, BlockPos blockPos, EnumFacing enumFacing) {
			TileEntity tile = world.getTileEntity(blockPos);
			if (cl.isInstance(tile)) {
				return supplier.apply((T) tile, enumFacing);
			} else {
				return null;
			}
		}

		@Nullable
		@Override
		public SerialProtocolDocumentationReference getDocumentationReference() {
			return null;
		}

		@Override
		public boolean isValid(World world, BlockPos blockPos, EnumFacing enumFacing, SerialInterface serialInterface) {
			return serialInterface instanceof SerialInterfaceTile && !((SerialInterfaceTile) serialInterface).tile.isInvalid();
		}
	}

	protected final T tile;

	protected SerialInterfaceTile(T tile) {
		this.tile = tile;
	}
}
