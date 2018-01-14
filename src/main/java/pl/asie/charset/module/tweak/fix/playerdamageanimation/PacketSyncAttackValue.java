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

package pl.asie.charset.module.tweak.fix.playerdamageanimation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.network.PacketEntity;

public class PacketSyncAttackValue extends PacketEntity {
	private float attackedAtYaw;

	public PacketSyncAttackValue() {
		super();
	}

	public PacketSyncAttackValue(Entity entity) {
		super(entity);
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeFloat(((EntityLivingBase) entity).attackedAtYaw);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		attackedAtYaw = buf.readFloat();
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);
		((EntityLivingBase) entity).attackedAtYaw = attackedAtYaw;
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
