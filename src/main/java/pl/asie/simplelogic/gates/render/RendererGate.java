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

import java.util.*;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelStateComposition;

import javax.vecmath.Vector3f;

import pl.asie.simplelogic.gates.BlockGate;
import pl.asie.simplelogic.gates.ItemGate;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;

public class RendererGate extends ModelFactory<PartGate> {
	public static final RendererGate INSTANCE = new RendererGate();

	private static final ModelRotation[] ROTATIONS_SIDE = {
			ModelRotation.X0_Y0, ModelRotation.X180_Y0,
			ModelRotation.X270_Y0, ModelRotation.X270_Y180,
			ModelRotation.X270_Y270, ModelRotation.X270_Y90
	};

	private static final ModelRotation[] ROTATIONS_TOP = {
			ModelRotation.X0_Y0, ModelRotation.X0_Y0,
			ModelRotation.X0_Y0, ModelRotation.X0_Y180,
			ModelRotation.X0_Y270, ModelRotation.X0_Y90
	};

	private static final Map<String, IModel> layerModels = new HashMap<String, IModel>();

	public RendererGate() {
		super(BlockGate.PROPERTY, new ResourceLocation("simplelogic:blocks/gate_bottom"));
		addDefaultBlockTransforms();
		addThirdPersonTransformation(getTransformation(0, 2.5f, 2.75f, 75, 45, 0, 0.375f));
	}

	@Override
	public IBakedModel bake(PartGate gate, boolean isItem, BlockRenderLayer blockRenderLayer) {
		SimpleBakedModel result = new SimpleBakedModel(this);
		ModelStateComposition transform = new ModelStateComposition(
				new TRSRTransformation(ROTATIONS_SIDE[gate.getSide().ordinal()]),
				new TRSRTransformation(ROTATIONS_TOP[gate.getTop().ordinal()])
		);

		if (gate.mirrored) {
			transform = new ModelStateComposition(
					transform,
					new TRSRTransformation(
							null, null, new Vector3f(-1.0f, 1.0f, 1.0f), null
					)
			);
		}

		GateLogic logic = gate.logic;

		GateRenderDefinitions.Definition definition = GateRenderDefinitions.INSTANCE.getGateDefinition(SimpleLogicGates.getId(logic));
		if (definition == null) {
			result.addModel(ModelLoaderRegistry.getMissingModel().bake(transform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
			return result;
		}

		GateRenderDefinitions.BaseDefinition base = GateRenderDefinitions.INSTANCE.base;

		IModel model = definition.getModel(gate.getModelName());
		if (model != null) {
			result.addModel(model.bake(transform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
		}
		IModel layerModel = definition.getModel("layer");

		int i = 0;

		for (GateRenderDefinitions.Layer layer : definition.layers) {
			GateLogic.State state = gate.logic.getLayerState(i++);
			if (state == GateLogic.State.NO_RENDER) {
				continue;
			}

			IModelState layerTransform = transform;

			if (layer.height != 0) {
				layerTransform = new ModelStateComposition(
						new TRSRTransformation(new Vector3f(0, (float) layer.height / 16f, 0), null, null, null),
						transform
				);
			}

			if ("color".equals(layer.type) && layer.texture != null) {
				model = layerModels.get(layer.texture);
				if (model == null) {
					model = layerModel.retexture(ImmutableMap.of("layer", layer.texture));
					layerModels.put(layer.texture, model);
				}

				IBakedModel bakedModel = model.bake(layerTransform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());

				int color = state == GateLogic.State.ON ? base.colorMul.get("on") :
						(state == GateLogic.State.OFF ? base.colorMul.get("off") : base.colorMul.get("disabled"));

				result.addModel(ModelTransformer.transform(bakedModel, SimpleLogicGates.blockGate.getDefaultState(), 0, ((quad, element, data) -> {
					if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
						return new float[] {
								((color) & 0xFF) / 255.0f,
								((color >> 8) & 0xFF) / 255.0f,
								((color >> 16) & 0xFF) / 255.0f,
								1
						};
					} else {
						return data;
					}
				})));
			} else if ("map".equals(layer.type) && layer.textures != null) {
				String texture = layer.textures.get(state.name().toLowerCase(Locale.ENGLISH));
				if (texture == null) {
					texture = layer.textures.get("off");
					if (texture == null) {
						texture = layer.textures.get("disabled");
					}
				}

				if (texture != null) {
					model = layerModels.get(texture);
					if (model == null) {
						model = layerModel.retexture(ImmutableMap.of("layer", texture));
						layerModels.put(texture, model);
					}

					result.addModel(model.bake(layerTransform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
				}
			}
		}

		Set<EnumFacing> invertedSides = EnumSet.noneOf(EnumFacing.class);

		i = 0;
		for (GateRenderDefinitions.Torch torch : definition.torches) {
			GateLogic.State state = gate.logic.getTorchState(i++);
			if (state == GateLogic.State.NO_RENDER) {
				continue;
			}

			if (torch.inverter != null) {
				EnumFacing inverter = EnumFacing.byName(torch.inverter);
				if (gate.logic.isSideInverted(inverter)) {
					invertedSides.add(inverter);
				} else {
					continue;
				}
			}

			result.addModel(
					definition.getModel(state == GateLogic.State.ON ? "torch_on" : "torch_off")
							.bake(new ModelStateComposition(
									transform, new TRSRTransformation(new Vector3f((torch.pos[0] - 7.5f) / 16.0f, 0f, (torch.pos[1] - 7.5f) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter())
			);
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (gate.logic.isSideInverted(facing) && !invertedSides.contains(facing)) {
				result.addModel(
						definition.getModel(gate.getInverterState(facing) ? "torch_on" : "torch_off")
								.bake(new ModelStateComposition(
										transform, new TRSRTransformation(new Vector3f(((facing.getFrontOffsetX() * 6.9875f)) / 16.0f, 0f, ((facing.getFrontOffsetZ() * 6.9875f)) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter())
				);
			}
		}

		return result;
	}

	@Override
	public PartGate fromItemStack(ItemStack stack) {
		return ItemGate.getPartGate(stack);
	}
}
