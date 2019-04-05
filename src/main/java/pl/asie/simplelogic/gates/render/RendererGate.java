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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.simplelogic.gates.*;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.simplelogic.gates.logic.GateRenderState;
import pl.asie.simplelogic.gates.logic.IGateContainer;

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

	private static final ModelStateComposition[] COMPOSITIONS;
	private static final Map<String, IModel> layerModels = new HashMap<String, IModel>();
	private final TRSRTransformation shiftGuiTransform;

	static {
		COMPOSITIONS = new ModelStateComposition[36];
		for (int i = 0; i < 36; i++) {
			COMPOSITIONS[i] = new ModelStateComposition(
					TRSRTransformation.from(ROTATIONS_SIDE[i / 6]),
					TRSRTransformation.from(ROTATIONS_TOP[i % 6])
			);
		}
	}

	public RendererGate() {
		super(BlockGate.PROPERTY, new ResourceLocation("minecraft:blocks/stone_slab_top"));
		addDefaultBlockTransforms();
		addThirdPersonTransformation(getTransformation(0, 2.5f, 2.75f, 75, 45, 0, 0.375f));
		shiftGuiTransform = getTransformation(0, -16, 0, 90, 0, 0, 1f);
	}

	@Override
	public boolean shouldCache(PartGate object, BlockRenderLayer layer) {
		return layer != null || SimpleLogicGatesClient.INSTANCE.getRenderer(object.logic.getClass()) != null;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		layerModels.clear();
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		if (cameraTransformType == ItemCameraTransforms.TransformType.GUI && GuiScreen.isShiftKeyDown()) {
			return ImmutablePair.of(this, shiftGuiTransform.getMatrix());
		} else {
			return super.handlePerspective(cameraTransformType);
		}
	}

	public ModelStateComposition getTransform(IGateContainer gate) {
		return COMPOSITIONS[gate.getSide().ordinal() * 6 + gate.getTop().ordinal()];
	}

	public boolean addBase(SimpleBakedModel result, ModelStateComposition transform, PartGate gate) {
		GateLogic logic = gate.logic;

		GateRenderDefinitions.Definition definition = GateRenderDefinitions.INSTANCE.getGateDefinition(SimpleLogicGates.getId(logic));
		if (definition == null) {
			result.addModel(ModelLoaderRegistry.getMissingModel().bake(transform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
			return false;
		} else {
			IModel model = definition.getModel(gate.getBaseModelName());
			if (model != null) {
				result.addModel(model.bake(transform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
			}
			return true;
		}
	}

	public void addLayers(SimpleBakedModel result, ModelStateComposition transform, PartGate gate, boolean isItem) {
		GateLogic logic = gate.logic;
		GateRenderDefinitions.Definition definition = GateRenderDefinitions.INSTANCE.getGateDefinition(SimpleLogicGates.getId(logic));
		GateRenderDefinitions.BaseDefinition base = GateRenderDefinitions.INSTANCE.base;

		IModel layerModel = definition.getModel(gate.getLayerModelName());
		IModel model;

		int i = 0;

		for (GateRenderDefinitions.Layer layer : definition.layers) {
			GateRenderState state = gate.logic.getLayerState(i++);
			if (state == GateRenderState.NO_RENDER) {
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
				String key = layer.texture; if (gate.mirrored) key = "m$" + key;
				model = layerModels.get(key);
				if (model == null) {
					model = layerModel.retexture(ImmutableMap.of("layer", layer.texture));
					layerModels.put(key, model);
				}

				IBakedModel bakedModel = model.bake(layerTransform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());

				int color = state == GateRenderState.ON ? base.colorMul.get("on") :
						(state == GateRenderState.OFF ? base.colorMul.get("off") : base.colorMul.get("disabled"));

				try {
					result.addModel(ModelTransformer.transform(bakedModel, SimpleLogicGates.blockGate.getDefaultState(), 0, ((quad, element, data) -> {
						if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
							return new float[]{
									((color) & 0xFF) / 255.0f,
									((color >> 8) & 0xFF) / 255.0f,
									((color >> 16) & 0xFF) / 255.0f,
									1
							};
						} else {
							return data;
						}
					})));
				} catch (ModelTransformer.TransformationFailedException e) {
					throw new RuntimeException(e);
				}
			} else if ("map".equals(layer.type) && layer.textures != null) {
				String texture = layer.textures.get(state.name().toLowerCase(Locale.ENGLISH));
				if (texture == null) {
					texture = layer.textures.get("off");
					if (texture == null) {
						texture = layer.textures.get("disabled");
					}
				}

				if (texture != null) {
					String key = texture; if (gate.mirrored) key = "m$" + key;
					model = layerModels.get(key);
					if (model == null) {
						model = layerModel.retexture(ImmutableMap.of("layer", texture));
						layerModels.put(key, model);
					}

					result.addModel(model.bake(layerTransform, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()));
				}
			}
		}

		Set<EnumFacing> invertedSides = EnumSet.noneOf(EnumFacing.class);

		i = 0;
		for (GateRenderDefinitions.Torch torch : definition.torches) {
			GateRenderState state;

			if (torch.inverter != null) {
				EnumFacing inverter = EnumFacing.byName(torch.inverter);
				if (gate.logic.isSideInverted(inverter)) {
					invertedSides.add(inverter);
					state = GateRenderState.bool(gate.getInverterState(inverter));
				} else {
					continue;
				}
			} else {
				state = gate.logic.getTorchState(i++);
				if (state == GateRenderState.NO_RENDER) {
					continue;
				}
			}

			String name = state == GateRenderState.ON ? (torch.model_on == null ? "torch_on" : torch.model_on) : (torch.model_off == null ? "torch_off" : torch.model_off);
			if (state == GateRenderState.DISABLED && torch.model_disabled != null) {
				name = torch.model_disabled;
			}

			if (name.length() > 0) {
				float xPos = (torch.pos[0] - 7.5f) / 16.0f;
				float zPos = (torch.pos[1] - 7.5f) / 16.0f;
				float yOffset = torch.pos.length >= 3 ? (torch.pos[2] / 16.0f) : 0;

				if (gate.mirrored) {
					xPos = -xPos;
				}

				IBakedModel torchModel = definition.getModel(name)
						.bake(new ModelStateComposition(
								transform, new TRSRTransformation(new Vector3f(xPos, yOffset, zPos), null, null, null)), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
				String torchColorStr = state == GateRenderState.ON ? torch.color_on : torch.color_off;
				if (torchColorStr != null) {
					int torchColor = Integer.parseInt(torchColorStr, 16);
					if ((torchColor & 0xFF000000) == 0) {
						torchColor |= 0xFF000000;
					}

					try {
						torchModel = ModelTransformer.transform(
								torchModel,
								SimpleLogicGates.blockGate.getDefaultState(),
								0L,
								ModelTransformer.IVertexTransformer.tint(torchColor)
						);
					} catch (ModelTransformer.TransformationFailedException e) {
						throw new RuntimeException(e);
					}
				}

				result.addModel(torchModel);
			}
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (gate.logic.isSideInverted(facing) && !invertedSides.contains(facing)) {
				result.addModel(
						definition.getModel(gate.getInverterState(facing) ? "torch_on" : "torch_off")
								.bake(new ModelStateComposition(
										transform, new TRSRTransformation(new Vector3f(((facing.getXOffset() * 6.9875f)) / 16.0f, 0f, ((facing.getZOffset() * 6.9875f)) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter())
				);
			}
		}

		GateCustomRenderer renderer = SimpleLogicGatesClient.INSTANCE.getRenderer(gate.logic.getClass());
		if (renderer != null) {
			appendStaticModels(renderer, gate, result, isItem);
		}
	}

	private void appendStaticModels(GateCustomRenderer renderer, PartGate gate, SimpleBakedModel model, boolean isItem) {
		renderer.renderStatic(gate, gate.logic, isItem, (m) -> model.addModel((IBakedModel) m), (q, f) -> model.addQuad((EnumFacing) f, (BakedQuad) q));
	}

	@Override
	public IBakedModel bake(PartGate gate, boolean isItem, BlockRenderLayer blockRenderLayer) {
		SimpleBakedModel result = new SimpleBakedModel(this);
		ModelStateComposition transform = getTransform(gate);

		if (!addBase(result, transform, gate)) {
			return result;
		}

		if (!SimpleLogicGates.useTESRs || isItem) {
			addLayers(result, transform, gate, isItem);
		}

		return result;
	}

	@Override
	public PartGate fromItemStack(ItemStack stack) {
		Optional<PartGate> g = ItemGate.getPartGate(stack);
		g.ifPresent((a) -> a.logic.updateOutputs(a));
		return g.orElse(null);
	}
}
