/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tweak.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.Utils;

/**
 * Created by asie on 1/2/17.
 */
public class PacketCarryGrab extends Packet {
	enum Type {
		BLOCK,
		ENTITY
	}

	private EntityPlayer player;
	private Type type;
	private World world;
	private BlockPos pos;
	private int dim, entityId;

	public PacketCarryGrab() {

	}

	public PacketCarryGrab(World world, BlockPos pos) {
		this.world = world;
		this.type = Type.BLOCK;
		this.pos = pos;
	}

	public PacketCarryGrab(World world, Entity entity) {
		this.world = world;
		this.type = Type.ENTITY;
		this.entityId = entity.getEntityId();
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		dim = buf.readInt();
		type = Type.values()[buf.readByte()];

		switch (type) {
			case BLOCK:
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				pos = new BlockPos(x, y, z);
				break;
			case ENTITY:
				entityId = buf.readInt();
				break;
		}
	}

	@Override
	public void apply(INetHandler handler) {
		player = getPlayer(handler);
		world = getWorld(handler, dim);

		if (player != null && world != null) {
			switch (type) {
				case BLOCK:
					CharsetTweakBlockCarrying.grabBlock(player, world, pos);
					break;
				case ENTITY:
					Entity entity = world.getEntityByID(entityId);
					if (entity != null) {
						CharsetTweakBlockCarrying.grabEntity(player, world, entity);
					}
					break;
			}
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeInt(world.provider.getDimension());
		switch (type) {
			case BLOCK:
				buf.writeByte(0);
				buf.writeInt(pos.getX());
				buf.writeInt(pos.getY());
				buf.writeInt(pos.getZ());
				break;
			case ENTITY:
				buf.writeByte(1);
				buf.writeInt(entityId);
				break;
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
