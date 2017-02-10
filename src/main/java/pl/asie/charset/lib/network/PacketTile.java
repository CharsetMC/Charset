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

package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.utils.Utils;

public abstract class PacketTile extends Packet {
	protected TileEntity tile;

	public PacketTile() {

	}

	public PacketTile(TileEntity tile) {
		this.tile = tile;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dim = buf.readInt();
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();

		World w = Utils.getLocalWorld(dim);

		if (w != null) {
			tile = w.getTileEntity(new BlockPos(x, y, z));
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(tile.getWorld().provider.getDimension());
		buf.writeInt(tile.getPos().getX());
		buf.writeInt(tile.getPos().getY());
		buf.writeInt(tile.getPos().getZ());
	}
}
