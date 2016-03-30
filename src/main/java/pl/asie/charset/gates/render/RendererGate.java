package pl.asie.charset.gates.render;

import java.util.*;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelStateComposition;

import javax.vecmath.Vector3f;
import pl.asie.charset.gates.ItemGate;
import pl.asie.charset.gates.PartGate;
import pl.asie.charset.lib.render.SimpleBakedModel;
import pl.asie.charset.lib.render.ModelFactory;
import pl.asie.charset.lib.utils.ClientUtils;

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
		super(PartGate.PROPERTY, new ResourceLocation("charsetgates:blocks/gate_bottom"));
		addDefaultBlockTransforms();
	}

	@Override
	public IBakedModel bake(PartGate gate) {
		SimpleBakedModel result = new SimpleBakedModel(this);
		ModelStateComposition transform = new ModelStateComposition(
				new TRSRTransformation(ROTATIONS_SIDE[gate.getSide().ordinal()]),
				new TRSRTransformation(ROTATIONS_TOP[gate.getTop().ordinal()])
		);

		if (gate.isMirrored()) {
			transform = new ModelStateComposition(
					transform,
					new TRSRTransformation(
							null, null, new Vector3f(-1.0f, 1.0f, 1.0f), null
					)
			);
		}

		GateRenderDefinitions.Definition definition = GateRenderDefinitions.INSTANCE.getGateDefinition(gate.getType());
		GateRenderDefinitions.BaseDefinition base = GateRenderDefinitions.INSTANCE.base;

		IModel model = definition.getModel(gate.getModelName());
		if (model != null) {
			result.addModel(model.bake(transform, DefaultVertexFormats.BLOCK, ClientUtils.textureGetter));
		}
		IRetexturableModel layerModel = (IRetexturableModel) definition.getModel("layer");

		int i = 0;

		for (GateRenderDefinitions.Layer layer : definition.layers) {
			PartGate.State state = gate.getLayerState(i++);
			if (state == PartGate.State.NO_RENDER) {
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

				IBakedModel bakedModel = model.bake(layerTransform, DefaultVertexFormats.BLOCK, ClientUtils.textureGetter);

				int color = state == PartGate.State.ON ? base.colorMul.get("on") :
						(state == PartGate.State.OFF ? base.colorMul.get("off") : base.colorMul.get("disabled"));

				result.addModel(bakedModel, color);
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

					result.addModel(model.bake(layerTransform, DefaultVertexFormats.BLOCK, ClientUtils.textureGetter));
				}
			}
		}

		Set<EnumFacing> invertedSides = EnumSet.noneOf(EnumFacing.class);

		i = 0;
		for (GateRenderDefinitions.Torch torch : definition.torches) {
			PartGate.State state = gate.getTorchState(i++);
			if (state == PartGate.State.NO_RENDER) {
				continue;
			}

			if (torch.inverter != null) {
				EnumFacing inverter = EnumFacing.byName(torch.inverter);
				if (gate.isSideInverted(inverter)) {
					invertedSides.add(inverter);
				} else {
					continue;
				}
			}

			result.addModel(
					definition.getModel(state == PartGate.State.ON ? "torch_on" : "torch_off")
							.bake(new ModelStateComposition(
									transform, new TRSRTransformation(new Vector3f((torch.pos[0] - 7.5f) / 16.0f, 0f, (torch.pos[1] - 7.5f) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ClientUtils.textureGetter)
			);
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (gate.isSideInverted(facing) && !invertedSides.contains(facing)) {
				result.addModel(
						definition.getModel(gate.getInverterState(facing) ? "torch_on" : "torch_off")
								.bake(new ModelStateComposition(
										transform, new TRSRTransformation(new Vector3f(((facing.getFrontOffsetX() * 7)) / 16.0f, 0f, ((facing.getFrontOffsetZ() * 7)) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ClientUtils.textureGetter)
				);
			}
		}

		return result;
	}

	@Override
	public PartGate fromItemStack(ItemStack stack) {
		return ItemGate.getPartGate(stack);
	}

	@Override
	public ItemOverrideList getOverrides() {
		addThirdPersonTransformation(getTransformation(0, 2.5f, 2.75f, 75, 45, 0, 0.375f));
		return super.getOverrides();
	}
}
