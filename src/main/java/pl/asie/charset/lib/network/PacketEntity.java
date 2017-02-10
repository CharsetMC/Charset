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
import net.minecraft.entity.Entity;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import pl.asie.charset.lib.utils.Utils;

public abstract class PacketEntity extends Packet {
	protected Entity entity;

	public PacketEntity() {

	}

	public PacketEntity(Entity entity) {
		this.entity = entity;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dim = buf.readInt();
		int id = buf.readInt();

		World w = Utils.getLocalWorld(dim);

		if (w != null) {
			entity = w.getEntityByID(id);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(entity.world.provider.getDimension());
		buf.writeInt(entity.getEntityId());
	}
}
