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

package pl.asie.charset.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.pipes.pipe.TilePipe;

public final class PipeUtils {
	private PipeUtils() {

	}

	public static TilePipe getPipe(TileEntity tile) {
		if (tile == null)
			return null;
		if (tile instanceof TilePipe)
			return (TilePipe) tile;
		if (tile.hasCapability(Capabilities.PIPE_VIEW, null))
			return (TilePipe) tile.getCapability(Capabilities.PIPE_VIEW, null);
		return null;
	}

	public static TilePipe getPipe(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(blockPos);
		return getPipe(tile);
	}
}
