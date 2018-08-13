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
import pl.asie.simplelogic.gates.logic.GateLogicBundledTransposer;
import pl.asie.simplelogic.gates.logic.GateLogicTimer;

public class PacketTransposerConnection extends PacketGate {
	private int from, to;
	private boolean remove;

	public PacketTransposerConnection() {

	}

	public PacketTransposerConnection(PartGate gate, int from, int to, boolean remove) {
		super(gate);
		this.from = from;
		this.to = to;
		this.remove = remove;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		from = buf.readUnsignedByte();
		to = buf.readUnsignedByte();

		remove = (to & 0x10) != 0;
		to &= 0x0F;
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeByte(from);
		buf.writeByte(to | (remove ? 16 : 0));
	}

	@Override
	public void applyGate(PartGate gate, EntityPlayer player) {
		if (!(gate.logic instanceof GateLogicBundledTransposer)) return;

		GateLogicBundledTransposer logic = (GateLogicBundledTransposer) gate.logic;
		int oldLTF = logic.transpositionMap[from];
		if (remove) {
			logic.transpositionMap[from] &= ~(1 << to);
		} else {
			logic.transpositionMap[from] |= (1 << to);
		}

		if (oldLTF != logic.transpositionMap[from]) {
			logic.onTMapChanged(gate);
		}
	}
}
