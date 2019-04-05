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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;

public class PacketSignalMeterData extends Packet {
	private static final ISimpleInstantiatingRegistry<ISignalMeterData> REGISTRY
			= CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(ISignalMeterData.class);
	private ISignalMeterData data;

	public PacketSignalMeterData() {

	}

	public PacketSignalMeterData(ISignalMeterData data) {
		this.data = data;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		int id = buf.readVarInt();
		data = REGISTRY.create(id);
		data.deserialize(buf);
	}

	@Override
	public void apply(INetHandler handler) {
		EntityPlayer player = getPlayer(handler);
		if (player != null && player.hasCapability(CharsetToolsEngineering.meterTrackerCap, null)) {
			player.getCapability(CharsetToolsEngineering.meterTrackerCap, null).setClientData(data);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeVarInt(REGISTRY.getId(data));
		data.serialize(buf);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
