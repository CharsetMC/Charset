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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;

public class SignalMeterTrackerEventHandler {
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side == Side.SERVER) {
			ItemStack mainHand = event.player.getHeldItemMainhand();
			ItemStack offHand = event.player.getHeldItemOffhand();
			if ((!mainHand.isEmpty() && mainHand.getItem() instanceof ItemSignalMeter)
				|| (!offHand.isEmpty() && offHand.getItem() instanceof ItemSignalMeter)) {

				if (event.player.hasCapability(CharsetToolsEngineering.meterTrackerCap, null)) {
					ISignalMeterTracker tracker = event.player.getCapability(CharsetToolsEngineering.meterTrackerCap, null);
					if (tracker instanceof SignalMeterTracker) {
						ISignalMeterData data = ((SignalMeterTracker) tracker).getNewDataToSend(event.player);
						if (data == null) {
							data = new SignalMeterDataDummy();
						}

//						if (data != null) {
						CharsetToolsEngineering.packet.sendTo(new PacketSignalMeterData(data), event.player);
//						}
					}
				}
			}
		}
	}
}
