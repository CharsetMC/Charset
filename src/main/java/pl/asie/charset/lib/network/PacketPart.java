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

import java.util.UUID;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fmp.multipart.IMultipart;
import net.minecraftforge.fmp.multipart.IMultipartContainer;
import net.minecraftforge.fmp.multipart.MultipartHelper;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.Packet;

public abstract class PacketPart extends Packet {
	protected IMultipart part;

	public PacketPart() {

	}

	public PacketPart(IMultipart part) {
		this.part = part;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dim = buf.readInt();
		int x = buf.readInt();
		int y = buf.readUnsignedShort();
		int z = buf.readInt();
		long l1 = buf.readLong();
		long l2 = buf.readLong();
		UUID id = new UUID(l1, l2);

		World w = ModCharsetLib.proxy.getLocalWorld(dim);

		if (w != null) {
			IMultipartContainer container = MultipartHelper.getPartContainer(w, new BlockPos(x, y, z));
			if (container != null) {
				part = container.getPartFromID(id);
			}
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(part.getWorld().provider.getDimension());
		buf.writeInt(part.getPos().getX());
		buf.writeShort(part.getPos().getY());
		buf.writeInt(part.getPos().getZ());
		UUID id = part.getContainer().getPartID(part);
		if (id == null) {
			// FIXME: TODO: HACK! HACK!
			id = UUID.randomUUID();
		}
		buf.writeLong(id.getMostSignificantBits());
		buf.writeLong(id.getLeastSignificantBits());
	}
}
