/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.simplelogic.gates.logic.IGateContainer;

import javax.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static pl.asie.simplelogic.gates.render.FastTESRGate.renderer;

public abstract class GateCustomRenderer<T extends GateLogic> {
	public abstract Class<T> getLogicClass();

	/**
	 * Make sure to override GateLogic.renderEquals/renderHashCode for cases where isItem = false!
	 */
	public void renderStatic(IGateContainer gate, T logic, boolean isItem, Consumer<IBakedModel> modelConsumer, BiConsumer<BakedQuad, EnumFacing> quadConsumer) {

	}

	public boolean hasDynamic() {
		return false;
	}

	public void renderDynamic(IGateContainer gate, T logic, IBlockAccess world, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {

	}

	// Utility methods

	protected final IBakedModel getTransformedModel(IBakedModel model, IGateContainer gate) throws ModelTransformer.TransformationFailedException {
		return getTransformedModel(model, null, gate);
	}

	protected final IBakedModel getTransformedModel(IBakedModel model, @Nullable ModelTransformer.IVertexTransformer transformer, IGateContainer gate) throws ModelTransformer.TransformationFailedException {
		ModelTransformer.IVertexTransformer rotate = ModelTransformer.IVertexTransformer.transform(RendererGate.INSTANCE.getTransform(gate), null);

		return ModelTransformer.transform(
				model, SimpleLogicGates.blockGate.getDefaultState(),
				0L,
				transformer != null ? ModelTransformer.IVertexTransformer.compose(transformer, rotate) : rotate
		);
	}

	protected final void renderTransformedModel(IBakedModel model, IGateContainer gate, IBlockAccess world, double x, double y, double z, BufferBuilder buffer) {
		renderTransformedModel(model, null, gate, world, x, y, z, buffer);
	}

	@SuppressWarnings("SameParameterValue")
	protected final void renderTransformedModel(IBakedModel model, @Nullable  ModelTransformer.IVertexTransformer transformer, IGateContainer gate, IBlockAccess world, double x, double y, double z, BufferBuilder buffer) {
		try {
			model = getTransformedModel(model, transformer, gate);
		} catch (ModelTransformer.TransformationFailedException e) {
			throw new RuntimeException(e);
		}
		BlockPos pos = gate.getGatePos();
		buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
		renderer.renderModel(world, model, SimpleLogicGates.blockGate.getDefaultState(), pos, buffer, false);
		buffer.setTranslation(0, 0, 0);
	}
}
