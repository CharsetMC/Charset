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

package pl.asie.charset.patchwork;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import java.util.function.Function;

public class LocksCapabilityHook {
	public static final Result NO = new Result(false);

	public static final class Result {
		private final Function<Object, Object> transformer;
		private final boolean result;

		public Result(boolean b) {
			this.transformer = null;
			this.result = b;
		}

		public Result(boolean b, Function<Object, Object> transformer) {
			this.transformer = transformer;
			this.result = b;
		}

		public boolean captures() {
			return result;
		}

		public boolean canApply() {
			return transformer != null;
		}

		public Object apply(Object o) {
			return transformer.apply(o);
		}
	}

	public interface Handler {
		Result wrapCapability(TileEntity tile, Capability capability, EnumFacing facing);
	}

	public static Handler handler = (tile, capability, facing) -> NO;
}
