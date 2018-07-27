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

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.logic.GateLogic;

import javax.annotation.Nullable;

import static pl.asie.simplelogic.gates.render.FastTESRGate.renderer;

public abstract class GateDynamicRenderer<T extends GateLogic> {
	public abstract Class<T> getLogicClass();

	protected final void renderTransformedModel(IBakedModel model, PartGate gate, IBlockAccess world, double x, double y, double z, BufferBuilder buffer) {
		renderTransformedModel(model, null, gate, world, x, y, z, buffer);
	}

	@SuppressWarnings("SameParameterValue")
	protected final void renderTransformedModel(IBakedModel model, @Nullable  ModelTransformer.IVertexTransformer transformer, PartGate gate, IBlockAccess world, double x, double y, double z, BufferBuilder buffer) {
		ModelTransformer.IVertexTransformer rotate = ModelTransformer.IVertexTransformer.transform(RendererGate.INSTANCE.getTransform(gate), null);

		model = ModelTransformer.transform(
				model, SimpleLogicGates.blockGate.getDefaultState(),
				0L,
				transformer != null ? ModelTransformer.IVertexTransformer.compose(transformer, rotate) : rotate
		);

		BlockPos pos = gate.getPos();
		buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
		renderer.renderModel(world, model, SimpleLogicGates.blockGate.getDefaultState(), pos, buffer, false);
		buffer.setTranslation(0, 0, 0);
	}

	public abstract void render(PartGate gate, T logic, IBlockAccess world, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer);
}
