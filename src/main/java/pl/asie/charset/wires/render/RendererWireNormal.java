package pl.asie.charset.wires.render;

import java.util.Collections;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireType;
import pl.asie.charset.wires.internal.WireLocation;

/**
 * Created by asie on 12/5/15.
 */
public class RendererWireNormal extends RendererWireBase {
	private final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][] {
			{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}
	};

	private final ModelRotation[] ROTATIONS = new ModelRotation[] {
			ModelRotation.X0_Y0,
			ModelRotation.X180_Y0,
			ModelRotation.X270_Y0,
			ModelRotation.X270_Y180,
			ModelRotation.X270_Y270,
			ModelRotation.X270_Y90
	};

	protected final FaceBakeryWire faceBakery = new FaceBakeryWire();
	protected final TextureAtlasSprite[] icons = new TextureAtlasSprite[getIconArraySize()];
	protected final String type;
	protected final int width, height;

	public RendererWireNormal(String type, int width, int height) {
		this.type = type;
		this.width = width;
		this.height = height;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
		return Collections.emptyList();
	}

	private int getRenderColor(TileWireContainer wire, WireLocation loc) {
		if (wire != null) {
			return wire.getRenderColor(loc);
		} else if (stack != null) {
			WireType t = WireType.VALUES[stack.getItemDamage() >> 1];
			return t.type() == WireType.Type.INSULATED ? EnumDyeColor.byMetadata(t.color()).getMapColor().colorValue : -1;
		} else {
			return -1;
		}
	}

	private boolean wc(TileWireContainer wire, EnumFacing dir) {
		return wire == null ? (transform == ItemCameraTransforms.TransformType.THIRD_PERSON ? dir.getAxis() == EnumFacing.Axis.Y : true) : wire.connects(WireLocation.FREESTANDING, dir);
	}

	protected int getIconArraySize() {
		return 4;
	}

	protected TextureAtlasSprite getIcon(boolean isCrossed, boolean lit, boolean isEdge, EnumFacing side) {
		return icons[(isCrossed ? 0 : 1) + (lit ? 2 : 0)];
	}

	protected void configureRenderer(boolean isTop, int cmc) {

	}

	public void addWireFreestanding(TileWireContainer wire, boolean lit, List<BakedQuad> quads) {
		float min = 8.0f - (width / 2);
		float max = 8.0f + (width / 2);
		Vector3f minX = new Vector3f(min, wc(wire, EnumFacing.DOWN) ? 0.0f : min, wc(wire, EnumFacing.NORTH) ? 0.0f : min);
		Vector3f maxX = new Vector3f(min, wc(wire, EnumFacing.UP) ? 16.0f : max, wc(wire, EnumFacing.SOUTH) ? 16.0f : max);
		Vector3f minY = new Vector3f(wc(wire, EnumFacing.WEST) ? 0.0f : min, min, wc(wire, EnumFacing.NORTH) ? 0.0f : min);
		Vector3f maxY = new Vector3f(wc(wire, EnumFacing.EAST) ? 16.0f : max, min, wc(wire, EnumFacing.SOUTH) ? 16.0f : max);
		Vector3f minZ = new Vector3f(wc(wire, EnumFacing.WEST) ? 0.0f : min, wc(wire, EnumFacing.DOWN) ? 0.0f : min, min);
		Vector3f maxZ = new Vector3f(wc(wire, EnumFacing.EAST) ? 16.0f : max, wc(wire, EnumFacing.UP) ? 16.0f : max, min);

		int cmcX = (wc(wire, EnumFacing.UP) ? 8 : 0) | (wc(wire, EnumFacing.DOWN) ? 4 : 0) | (wc(wire, EnumFacing.NORTH) ? 2 : 0) | (wc(wire, EnumFacing.SOUTH) ? 1 : 0);
		int cmcY = (wc(wire, EnumFacing.NORTH) ? 4 : 0) | (wc(wire, EnumFacing.SOUTH) ? 8 : 0) | (wc(wire, EnumFacing.WEST) ? 2 : 0) | (wc(wire, EnumFacing.EAST) ? 1 : 0);
		int cmcZ = (wc(wire, EnumFacing.UP) ? 8 : 0) | (wc(wire, EnumFacing.DOWN) ? 4 : 0) | (wc(wire, EnumFacing.WEST) ? 1 : 0) | (wc(wire, EnumFacing.EAST) ? 2 : 0);

		for (int i = 0; i < 2; i++) {
			configureRenderer(true, cmcX);

			quads.add(
				faceBakery.makeBakedQuad(
					minX, maxX, getRenderColor(wire, WireLocation.FREESTANDING), new float[] {minX.getZ(), minX.getY(), maxX.getZ(), maxX.getY()},
					getIcon(true, lit, false, i == 0 ? EnumFacing.WEST : EnumFacing.EAST), i == 0 ? EnumFacing.WEST : EnumFacing.EAST,
					ModelRotation.X0_Y0, true
				)
			);

			configureRenderer(true, cmcY);

			quads.add(
				faceBakery.makeBakedQuad(
					minY, maxY, getRenderColor(wire, WireLocation.FREESTANDING), new float[]{minY.getX(), minY.getZ(), maxY.getX(), maxY.getZ()},
					getIcon(true, lit, false, i == 0 ? EnumFacing.DOWN : EnumFacing.UP), i == 0 ? EnumFacing.DOWN : EnumFacing.UP,
					ModelRotation.X0_Y0, true
				)
			);

			configureRenderer(true, cmcZ);

			quads.add(
				faceBakery.makeBakedQuad(
					minZ, maxZ, getRenderColor(wire, WireLocation.FREESTANDING), new float[]{minZ.getX(), minZ.getY(), maxZ.getX(), maxZ.getY()},
					getIcon(true, lit, false, i == 0 ? EnumFacing.NORTH : EnumFacing.SOUTH), i == 0 ? EnumFacing.NORTH : EnumFacing.SOUTH,
					ModelRotation.X0_Y0, true
				)
			);

			if (i == 0) {
				// set to max
				minX.setX(max);
				maxX.setX(max);
				minY.setY(max);
				maxY.setY(max);
				minZ.setZ(max);
				maxZ.setZ(max);

				// swap
				cmcY = (cmcY & 0x3) | ((cmcY & 0x8) >> 1) | ((cmcY & 0x4) << 1);
				cmcX = (cmcX & 0xC) | ((cmcX & 0x2) >> 1) | ((cmcX & 0x1) << 1);
				cmcZ = (cmcZ & 0xC) | ((cmcZ & 0x2) >> 1) | ((cmcZ & 0x1) << 1);
			}
		}

		configureRenderer(true, 0);

		for (EnumFacing f : EnumFacing.VALUES) {
			if (wc(wire, f)) {
				quads.add(
						faceBakery.makeBakedQuad(
								new Vector3f(min, 0.0F, min), new Vector3f(max, 0.0f, max),
								getRenderColor(wire, WireLocation.FREESTANDING), new float[] {min, min, max, max},
								getIcon(false, lit, true, f), EnumFacing.DOWN, ROTATIONS[f.ordinal()], false
						)
				);
			}
		}

		configureRenderer(false, 0);
	}

	public void addWire(TileWireContainer wire, WireLocation side, boolean lit, List<BakedQuad> quads) {
		if (side == WireLocation.FREESTANDING) {
			addWireFreestanding(wire, lit, quads);
			return;
		}

		float min = 8.0f - (width / 2);
		float max = 8.0f + (width / 2);
		float minH = 0.0f;
		float maxH = height;
		
		boolean[] connectionMatrix = new boolean[] {
				wire == null ? true : wire.connectsAny(side, CONNECTION_DIRS[side.ordinal()][0]),
				wire == null ? true : wire.connectsAny(side, CONNECTION_DIRS[side.ordinal()][1]),
				wire == null ? true : wire.connectsAny(side, CONNECTION_DIRS[side.ordinal()][2]),
				wire == null ? true : wire.connectsAny(side, CONNECTION_DIRS[side.ordinal()][3])
		};
		int cmc = (connectionMatrix[0] ? 8 : 0) | (connectionMatrix[1] ? 4 : 0) | (connectionMatrix[2] ? 2 : 0) | (connectionMatrix[3] ? 1 : 0);

 		boolean[] cornerConnectionMatrix = new boolean[] {
				wire == null ? true : wire.connectsCorner(side, CONNECTION_DIRS[side.ordinal()][0]),
				wire == null ? true : wire.connectsCorner(side, CONNECTION_DIRS[side.ordinal()][1]),
				wire == null ? true : wire.connectsCorner(side, CONNECTION_DIRS[side.ordinal()][2]),
				wire == null ? true : wire.connectsCorner(side, CONNECTION_DIRS[side.ordinal()][3])
		};

		ModelRotation rot = ROTATIONS[side.ordinal()];

		// Center face

		Vector3f from = new Vector3f(min, height, min);
		Vector3f to = new Vector3f(max, height, max);

		if (connectionMatrix[0]) {
			from.setZ(0.0f);
		}
		if (connectionMatrix[1]) {
			to.setZ(16.0f);
		}
		if (connectionMatrix[2]) {
			from.setX(0.0f);
		}
		if (connectionMatrix[3]) {
			to.setX(16.0f);
		}

		configureRenderer(true, cmc);

		quads.add(
				faceBakery.makeBakedQuad(
						from, to,
						getRenderColor(wire, side), new float[] {from.getX(), from.getZ(), to.getX(), to.getZ()},
						getIcon(true, lit, false, EnumFacing.UP), EnumFacing.UP,
						rot, true
				)
		);

		from.setY(0.0F);
		to.setY(0.0F);
		quads.add(
				faceBakery.makeBakedQuad(
						from, to,
						getRenderColor(wire, side), new float[] {from.getX(), from.getZ(), to.getX(), to.getZ()},
						getIcon(true, lit, false, EnumFacing.DOWN), EnumFacing.DOWN, rot, true
				)
		);

		configureRenderer(false, 0);

		// Side faces
		Vector3f fromZ = new Vector3f(from.getX(), 0.0f, min);
		Vector3f toZ = new Vector3f(to.getX(), height, min);
		Vector3f fromX = new Vector3f(min, 0.0f, from.getZ());
		Vector3f toX = new Vector3f(min, height, to.getZ());

		quads.add(
			faceBakery.makeBakedQuad(
				fromX, toX,
				getRenderColor(wire, side), new float[] {fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()},
				getIcon(false, lit, false, EnumFacing.WEST), EnumFacing.WEST, rot, false
			)
		);

		quads.add(
			faceBakery.makeBakedQuad(
				fromZ, toZ,
				getRenderColor(wire, side), new float[] {fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()},
				getIcon(false, lit, false, EnumFacing.NORTH), EnumFacing.NORTH, rot, false
			)
		);

		fromX.setX(max);
		toX.setX(max);

		fromZ.setZ(max);
		toZ.setZ(max);

		quads.add(
			faceBakery.makeBakedQuad(
				fromX, toX,
				getRenderColor(wire, side), new float[] {fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()},
				getIcon(false, lit, false, EnumFacing.EAST), EnumFacing.EAST, rot, false
			)
		);

		quads.add(
			faceBakery.makeBakedQuad(
				fromZ, toZ,
				getRenderColor(wire, side), new float[] {fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()},
				getIcon(false, lit, false, EnumFacing.SOUTH), EnumFacing.SOUTH, rot, false
			)
		);

		// Edge faces
		float[] edgeUV = new float[] {min, minH, max, maxH};

		if (connectionMatrix[0]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(min, minH, 0.0F), new Vector3f(max, maxH, 0.0F),
					getRenderColor(wire, side), edgeUV,
					getIcon(false, lit, true, EnumFacing.NORTH), EnumFacing.NORTH, rot, false
				)
			);
		}

		if (connectionMatrix[1]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(min, minH, 16.0F), new Vector3f(max, maxH, 16.0F),
					getRenderColor(wire, side), edgeUV,
						getIcon(false, lit, true, EnumFacing.SOUTH), EnumFacing.SOUTH, rot, false
				)
			);
		}

		if (connectionMatrix[2]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(0.0F, minH, min), new Vector3f(0.0F, maxH, max),
					getRenderColor(wire, side), edgeUV,
						getIcon(false, lit, true, EnumFacing.WEST), EnumFacing.WEST, rot, false
				)
			);
		}

		if (connectionMatrix[3]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(16.0F, minH, min), new Vector3f(16.0F, maxH, max),
					getRenderColor(wire, side), edgeUV,
						getIcon(false, lit, true, EnumFacing.EAST), EnumFacing.EAST, rot, false
				)
			);
		}
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return null;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return icons[0];
	}

	public void loadTextures(TextureMap map) {
		icons[0] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_unlit_cross"));
		icons[1] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_unlit_full"));
		icons[2] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_lit_cross"));
		icons[3] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_lit_full"));
	}
}
