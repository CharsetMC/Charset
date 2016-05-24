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

package pl.asie.charset.lib;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class TileBuffer {
	private final TileEntity owner;
	private final TileEntity[] tiles;
	private World world;
	private boolean initialized;

	public TileBuffer(TileEntity owner) {
		this.owner = owner;
		this.tiles = new TileEntity[6];
	}

	public TileEntity getOwner() {
		return owner;
	}

	public TileEntity getTileEntity(EnumFacing side) {
		if (side != null) {
			if (tiles[side.ordinal()] != null && tiles[side.ordinal()].isInvalid()) {
				updateSide(side, true);
			}

			return tiles[side.ordinal()];
		} else {
			return null;
		}
	}

	private void updateSide(EnumFacing direction, boolean force) {
		int i = direction.ordinal();
		BlockPos pos = owner.getPos().offset(direction);

		if (!force) {
			if (tiles[i] != null && !tiles[i].isInvalid()) {
				return;
			}
		}

		tiles[i] = null;

		if (world == null) {
			world = owner.getWorld();
		}

		if (!force && !world.isBlockLoaded(pos)) {
			return;
		}

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.hasTileEntity(state)) {
			tiles[i] = world.getTileEntity(pos);
		}
	}

	public void update(boolean force) {
		for (EnumFacing direction : EnumFacing.VALUES) {
			updateSide(direction, force);
		}
	}
}
