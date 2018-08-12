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

package pl.asie.simplelogic.gates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import pl.asie.simplelogic.gates.logic.GateLogicTimer;

public class PacketTimerChangeTT extends PacketGate {
	private int change;

	public PacketTimerChangeTT() {

	}

	public PacketTimerChangeTT(PartGate gate, int change) {
		super(gate);
		this.change = change;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		change = buf.readInt();
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeInt(change);
	}

	@Override
	public void applyGate(PartGate gate, EntityPlayer player) {
		if (!(gate.logic instanceof GateLogicTimer)) return;

		GateLogicTimer glt = (GateLogicTimer) gate.logic;
		glt.setTicksTotal(gate, glt.getTicksTotal() + change);
	}
}
