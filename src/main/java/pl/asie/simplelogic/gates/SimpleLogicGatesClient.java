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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pl.asie.simplelogic.gates.gui.ContainerGate;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.simplelogic.gates.render.GateCustomRenderer;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class SimpleLogicGatesClient {
	public static final SimpleLogicGatesClient INSTANCE = new SimpleLogicGatesClient();
	private final Map<Class<? extends GateLogic>, Function<ContainerGate, GuiScreen>> guis = new IdentityHashMap<>();
	private final Map<Class<? extends GateLogic>, GateCustomRenderer> dynamicRenderers = new IdentityHashMap<>();

	@SuppressWarnings("unchecked")
	public void registerRenderer(GateCustomRenderer renderer) {
		dynamicRenderers.put(renderer.getLogicClass(), renderer);
	}

	@SuppressWarnings("unchecked")
	public void registerGui(Class<? extends GateLogic> cls, Function<ContainerGate, GuiScreen> func) {
		guis.put(cls, func);
	}

	public void openGui(PartGate gate) {
		FMLCommonHandler.instance().showGuiScreen(guis.get(gate.logic.getClass()).apply(new ContainerGate(Minecraft.getMinecraft().player.inventory, gate)));
	}

	public GateCustomRenderer getRenderer(Class<? extends GateLogic> cl) {
		return dynamicRenderers.get(cl);
	}
}
