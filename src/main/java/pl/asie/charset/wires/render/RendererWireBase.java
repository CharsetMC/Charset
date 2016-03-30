package pl.asie.charset.wires.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.model.IPerspectiveAwareModel;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.wires.logic.PartWireBase;

/**
 * Created by asie on 12/5/15.
 */
public abstract class RendererWireBase /* implements ISmartMultipartModel, ISmartItemModel, IPerspectiveAwareModel */ {
/*	protected IBlockState state;
	protected ItemStack stack;
	protected ItemCameraTransforms.TransformType transform;
	private final Map<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap = new HashMap<ItemCameraTransforms.TransformType, TRSRTransformation>();

	public RendererWireBase() {
		transformMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON, new TRSRTransformation(
				new Vector3f(0, 0, -2.75f / 16),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)),
				new Vector3f(0.375f, 0.375f, 0.375f),
				null));
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		this.state = null;
		this.stack = stack;
		return this;
	}

	@Override
	public IBakedModel handlePartState(IBlockState state) {
		this.state = state;
		this.stack = null;
		return this;
	}

	public void addWireFreestanding(PartWireBase wire, boolean lit, List<BakedQuad> quads) {

	}

	public void addWire(PartWireBase wire, WireFace side, boolean lit, List<BakedQuad> quads) {

	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
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
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	public abstract void loadTextures(TextureMap map);

	@Override
	public VertexFormat getFormat() {
		return DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;
	}

	@Override
	public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		this.transform = cameraTransformType;
		return new ImmutablePair<RendererWireBase, Matrix4f>(this,
				transformMap.containsKey(cameraTransformType) ? transformMap.get(cameraTransformType).getMatrix() : TRSRTransformation.identity().getMatrix());
	} */
}
