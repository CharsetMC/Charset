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

package pl.asie.simplelogic.gates.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pl.asie.charset.lib.network.PacketTile;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;

public abstract class PacketGate extends PacketTile {
	private EnumFacing facing;

	public PacketGate() {
		super();
	}

	public PacketGate(PartGate gate) {
		super(gate);
		facing = gate.getSide();
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		facing = EnumFacing.byIndex(buf.readByte());
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeByte(facing.ordinal());
	}

	@Override
	public final void apply(INetHandler handler) {
		super.apply(handler);
		if (tile.hasCapability(SimpleLogicGates.GATE_CAP, facing)) {
			applyGate(tile.getCapability(SimpleLogicGates.GATE_CAP, facing), getPlayer(handler));
		}
	}

	public abstract void applyGate(PartGate gate, EntityPlayer player);

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
