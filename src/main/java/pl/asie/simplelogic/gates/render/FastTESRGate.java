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

package pl.asie.simplelogic.gates.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.client.model.animation.FastTESR;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.wires.CharsetLibWires;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.SimpleLogicGatesClient;
import pl.asie.simplelogic.gates.logic.GateLogic;

public class FastTESRGate extends FastTESR<PartGate> {
	protected static BlockModelRenderer renderer;

	@Override
	public void renderTileEntityFast(PartGate te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		GateDynamicRenderer gdr = SimpleLogicGatesClient.INSTANCE.getDynamicRenderer(te.logic.getClass());

		if (SimpleLogicGates.useTESRs || gdr != null) {
			if (renderer == null) {
				renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
			}

			BlockPos pos = te.getPos();

			if (SimpleLogicGates.useTESRs) {
				SimpleBakedModel result = new SimpleBakedModel(RendererGate.INSTANCE);
				ModelStateComposition transform = RendererGate.INSTANCE.getTransform(te);
				RendererGate.INSTANCE.addLayers(result, transform, te);

				buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
				renderer.renderModel(getWorld(), result, SimpleLogicGates.blockGate.getDefaultState(), pos, buffer, false);
				buffer.setTranslation(0, 0, 0);
			}

			if (gdr != null) {
				//noinspection unchecked
				gdr.render(
						te, te.logic, getWorld(), x, y, z, partialTicks, destroyStage, partial, buffer
				);
			}
		}
	}
}
