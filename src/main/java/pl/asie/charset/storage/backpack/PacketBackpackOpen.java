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

package pl.asie.charset.storage.backpack;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.PacketEntity;
import pl.asie.charset.storage.ModCharsetStorage;

/**
 * Created by asie on 1/12/16.
 */
public class PacketBackpackOpen extends PacketEntity {
	public PacketBackpackOpen() {

	}

	public PacketBackpackOpen(EntityPlayer player) {
		super(player);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).openGui(ModCharsetStorage.instance, 2, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ);
		}
	}
}
