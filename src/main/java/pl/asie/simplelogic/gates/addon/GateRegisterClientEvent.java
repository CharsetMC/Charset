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

package pl.asie.simplelogic.gates.addon;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.SimpleLogicGatesClient;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.simplelogic.gates.render.GateDynamicRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GateRegisterClientEvent extends Event {
	public GateRegisterClientEvent() {

	}

	public void registerDynamicRenderer(GateDynamicRenderer renderer) {
		SimpleLogicGatesClient.INSTANCE.registerDynamicRenderer(renderer);
	}
}
