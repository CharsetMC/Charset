package pl.asie.charset.gates.render;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import mcmultipart.client.multipart.ISmartMultipartModel;
import pl.asie.charset.gates.ItemGate;
import pl.asie.charset.gates.PartGate;
import pl.asie.charset.lib.utils.ClientUtils;

public class RendererGate implements ISmartMultipartModel, ISmartItemModel, IPerspectiveAwareModel {
	public static final RendererGate INSTANCE = new RendererGate();

	private static final Map<ItemCameraTransforms.TransformType, TRSRTransformation> TRANSFORM_MAP = new HashMap<ItemCameraTransforms.TransformType, TRSRTransformation>();

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

	public final PartGate gate;
	private final IModelState transform;
	private final List<IBakedModel> bakedModels = new ArrayList<IBakedModel>();
	private final Map<IBakedModel, Integer> bakedModelsRecolor = new HashMap<IBakedModel, Integer>();

	static {
		TRANSFORM_MAP.put(ItemCameraTransforms.TransformType.THIRD_PERSON, new TRSRTransformation(
				new Vector3f(0, 0, -2.75f / 16),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
				new Vector3f(0.375f, 0.375f, 0.375f),
				null));
	}

	private RendererGate(PartGate gate) {
		this.gate = gate;
		ModelStateComposition transformPre = new ModelStateComposition(
				new TRSRTransformation(ROTATIONS_SIDE[gate.getSide().ordinal()]),
				new TRSRTransformation(ROTATIONS_TOP[gate.getTop().ordinal()])
		);

		if (gate.isMirrored()) {
			this.transform = new ModelStateComposition(
					transformPre,
					new TRSRTransformation(
							null, null, new Vector3f(-1.0f, 1.0f, 1.0f), null
					)
			);
		} else {
			this.transform = transformPre;
		}

		GateRenderDefinitions.Definition definition = GateRenderDefinitions.INSTANCE.getGateDefinition(gate.getType());
		GateRenderDefinitions.BaseDefinition base = GateRenderDefinitions.INSTANCE.base;

		IModel model = definition.getModel(gate.getModelName());
		if (model != null) {
			this.bakedModels.add(model.bake(transform, DefaultVertexFormats.BLOCK, ClientUtils.textureGetter));
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

				bakedModelsRecolor.put(bakedModel, color);
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

					bakedModels.add(model.bake(layerTransform, DefaultVertexFormats.BLOCK, ClientUtils.textureGetter));
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

			this.bakedModels.add(
					definition.getModel(state == PartGate.State.ON ? "torch_on" : "torch_off")
							.bake(new ModelStateComposition(
									transform, new TRSRTransformation(new Vector3f((torch.pos[0] - 7.5f) / 16.0f, 0f, (torch.pos[1] - 7.5f) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ClientUtils.textureGetter)
			);
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (gate.isSideInverted(facing) && !invertedSides.contains(facing)) {
				this.bakedModels.add(
						definition.getModel(gate.getInverterState(facing) ? "torch_on" : "torch_off")
								.bake(new ModelStateComposition(
										transform, new TRSRTransformation(new Vector3f(((facing.getFrontOffsetX() * 7)) / 16.0f, 0f, ((facing.getFrontOffsetZ() * 7)) / 16.0f), null, null, null)), DefaultVertexFormats.BLOCK, ClientUtils.textureGetter)
				);
			}
		}
	}

	private RendererGate() {
		this.gate = null;
		this.transform = null;
	}

	@Override
	public IBakedModel handlePartState(IBlockState state) {
		if (state instanceof IExtendedBlockState) {
			PartGate partGate = ((IExtendedBlockState) state).getValue(PartGate.PROPERTY);
			if (partGate != null) {
				return new RendererGate(partGate);
			}
		}
		return null;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		PartGate partGate = ItemGate.getPartGate(stack);
		if (partGate != null) {
			return new RendererGate(partGate);
		}
		return null;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing) {
		List<BakedQuad> list = Lists.newArrayList();
		for (IBakedModel model : bakedModels) {
			list.addAll(model.getFaceQuads(facing));
		}
		for (IBakedModel model : bakedModelsRecolor.keySet()) {
			ClientUtils.addRecoloredQuads(model.getFaceQuads(facing), bakedModelsRecolor.get(model), list, gate.getSide().getOpposite());
		}
		return list;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		List<BakedQuad> list = Lists.newArrayList();
		for (IBakedModel model : bakedModels) {
			list.addAll(model.getGeneralQuads());
		}
		for (IBakedModel model : bakedModelsRecolor.keySet()) {
			ClientUtils.addRecoloredQuads(model.getGeneralQuads(), bakedModelsRecolor.get(model), list, gate.getSide().getOpposite());
		}
		return list;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return ClientUtils.textureGetter.apply(new ResourceLocation("charsetgates:blocks/gate_bottom"));
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		return new ImmutablePair<RendererGate, Matrix4f>(this,
				TRANSFORM_MAP.containsKey(cameraTransformType) ? TRANSFORM_MAP.get(cameraTransformType).getMatrix() : TRSRTransformation.identity().getMatrix());
	}

	@Override
	public VertexFormat getFormat() {
		return DefaultVertexFormats.BLOCK;
	}
}
