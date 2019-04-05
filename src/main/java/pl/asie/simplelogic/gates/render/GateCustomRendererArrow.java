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

package pl.asie.simplelogic.gates.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.simplelogic.gates.logic.IArrowGateLogic;
import pl.asie.simplelogic.gates.logic.IGateContainer;

public abstract class GateCustomRendererArrow<T extends GateLogic> extends GateCustomRenderer<T> {
	public static IModel arrowModel;

	@Override
	public boolean hasDynamic() {
		return true;
	}

	@Override
	public void renderDynamic(IGateContainer gate, T logic, IBlockAccess world, double xpos, double ypos, double zpos, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		if (arrowModel == null || !(logic instanceof IArrowGateLogic)) {
			return;
		}

		float v = -(((IArrowGateLogic) logic).getArrowPosition() + ((IArrowGateLogic) logic).getArrowRotationDelta() * partialTicks);

		IBakedModel baked = arrowModel.bake(
				TRSRTransformation.identity(),
				DefaultVertexFormats.BLOCK,
				ModelLoader.defaultTextureGetter()
		);

		float factor = (float) (v*Math.PI*2);

		renderTransformedModel(
				baked,
				(quad, element, data) -> {
					float factorf;
					switch (element.getUsage()) {
						case POSITION:
							factorf = 0.5f;
							break;
						case NORMAL:
							factorf = 0.0f;
							break;
						default:
							return data;
					}

					float x = data[0] - factorf;
					float z = data[2] - factorf;

					return new float[] {
							z * MathHelper.sin(factor) + x * MathHelper.cos(factor) + factorf,
							data[1],
							z * MathHelper.cos(factor) - x * MathHelper.sin(factor) + factorf,
							data[3]
					};
				},
				gate,
				world, xpos, ypos, zpos, buffer
		);
	}
}
