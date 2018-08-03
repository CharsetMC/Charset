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
import pl.asie.simplelogic.gates.logic.GateLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GateRegisterEvent extends Event {
	private final List<PartGate> gateStacks = new ArrayList<>();

	public GateRegisterEvent() {

	}

	// For SimpleLogicGates.
	public List<PartGate> getGateStackPartList() {
		return Collections.unmodifiableList(gateStacks);
	}

	public void registerLogicType(ResourceLocation id, Class<? extends GateLogic> cl) {
		registerLogicType(id, cl, new ResourceLocation(id.getNamespace(), "gatedefs/" + id.getPath() + ".json"),
				"tile." + id.getNamespace() + ".gate." + id.getPath());
	}

	public void registerLogicType(ResourceLocation id, Class<? extends GateLogic> cl,
	                              ResourceLocation gateDefinition,
	                              String unlocalizedName) {
		SimpleLogicGates.INSTANCE.registerGate(id, cl, gateDefinition, unlocalizedName);
	}

	public void registerPartForCreativeTab(PartGate part) {
		gateStacks.add(part);
	}

}
