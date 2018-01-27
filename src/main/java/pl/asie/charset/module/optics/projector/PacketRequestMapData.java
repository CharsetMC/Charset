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

package pl.asie.charset.module.optics.projector;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.storage.MapData;
import pl.asie.charset.lib.network.Packet;

public class PacketRequestMapData extends Packet {
	private static final TIntLongMap requestTimes = new TIntLongHashMap();
	private int mapId;

	public PacketRequestMapData() {

	}

	public PacketRequestMapData(ItemStack stack) {
		mapId = stack.getMetadata();
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		mapId = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		EntityPlayer player = getPlayer(handler);
		if (player instanceof EntityPlayerMP) {
			ItemStack stack = new ItemStack(Items.FILLED_MAP, 1, mapId);
			MapData data = Items.FILLED_MAP.getMapData(stack, player.getEntityWorld());

			// this is probably wrong
			ItemStack oldStack = player.inventory.getStackInSlot(0);
			player.inventory.setInventorySlotContents(0, stack);
			data.updateVisiblePlayers(player, stack);
			player.inventory.setInventorySlotContents(0, oldStack);

			net.minecraft.network.Packet<?> packet = ((ItemMap) Items.FILLED_MAP).createMapDataPacket(
					stack,
					player.getEntityWorld(),
					player
			);

			if (packet != null) {
				((EntityPlayerMP) player).connection.sendPacket(packet);
			}
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeInt(mapId);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}

	public static void requestMap(ItemStack stack) {
		long time = System.currentTimeMillis();
		if (requestTimes.containsKey(stack.getMetadata()) && requestTimes.get(stack.getMetadata()) < time) {
			return;
		}

		requestTimes.put(stack.getMetadata(), time + 5000);
		CharsetProjector.packet.sendToServer(new PacketRequestMapData(stack));
	}
}
