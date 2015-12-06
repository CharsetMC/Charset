package pl.asie.charset.wires.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.property.IExtendedBlockState;

import pl.asie.charset.wires.ItemWire;
import pl.asie.charset.wires.TileWire;

/**
 * Created by asie on 12/5/15.
 */
public class RendererWireNormal extends RendererWireBase {
	private final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][] {
			{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST},
			{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH},
			{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.NORTH}
	};

	private final ModelRotation[] ROTATIONS = new ModelRotation[] {
			ModelRotation.X0_Y0,
			ModelRotation.X180_Y0,
			ModelRotation.X90_Y180,
			ModelRotation.X90_Y0,
			ModelRotation.X90_Y90,
			ModelRotation.X90_Y270
	};

	private final FaceBakery faceBakery = new FaceBakery();
	private final TextureAtlasSprite[] icons = new TextureAtlasSprite[4];
	private final String type;
	private final int width;

	public RendererWireNormal(String type, int width) {
		this.type = type;
		this.width = width;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
		return Collections.emptyList();
	}

	private boolean wc(TileWire wire, EnumFacing dir) {
		return wire == null ? (transform == ItemCameraTransforms.TransformType.THIRD_PERSON ? dir.getAxis() == EnumFacing.Axis.Y : false) : wire.connects(TileWire.WireSide.FREESTANDING, dir);
	}

	public void addWireFreestanding(TileWire wire, boolean lit, List<BakedQuad> quads) {
		float min = 8.0f - (width / 2);
		float max = 8.0f + (width / 2);
		Vector3f minX = new Vector3f(min, wc(wire, EnumFacing.DOWN) ? 0.0f : min, wc(wire, EnumFacing.NORTH) ? 0.0f : min);
		Vector3f maxX = new Vector3f(min, wc(wire, EnumFacing.UP) ? 16.0f : max, wc(wire, EnumFacing.SOUTH) ? 16.0f : max);
		Vector3f minY = new Vector3f(wc(wire, EnumFacing.WEST) ? 0.0f : min, min, wc(wire, EnumFacing.NORTH) ? 0.0f : min);
		Vector3f maxY = new Vector3f(wc(wire, EnumFacing.EAST) ? 16.0f : max, min, wc(wire, EnumFacing.SOUTH) ? 16.0f : max);
		Vector3f minZ = new Vector3f(wc(wire, EnumFacing.WEST) ? 0.0f : min, wc(wire, EnumFacing.DOWN) ? 0.0f : min, min);
		Vector3f maxZ = new Vector3f(wc(wire, EnumFacing.EAST) ? 16.0f : max, wc(wire, EnumFacing.UP) ? 16.0f : max, min);

		for (int i = 0; i < 2; i++) {
			quads.add(
				faceBakery.makeBakedQuad(
					minX, maxX,
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[]{minX.getZ(), minX.getY(), maxX.getZ(), maxX.getY()}, 0)),
					icons[lit ? 2 : 0], i == 0 ? EnumFacing.WEST : EnumFacing.EAST,
					ModelRotation.X0_Y0, null, true, true
				)
			);

			quads.add(
				faceBakery.makeBakedQuad(
					minY, maxY,
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[]{minY.getX(), minY.getZ(), maxY.getX(), maxY.getZ()}, 0)),
					icons[lit ? 2 : 0], i == 0 ? EnumFacing.DOWN : EnumFacing.UP,
					ModelRotation.X0_Y0, null, true, true
				)
			);

			quads.add(
				faceBakery.makeBakedQuad(
					minZ, maxZ,
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[]{minZ.getX(), minZ.getY(), maxZ.getX(), maxZ.getY()}, 0)),
					icons[lit ? 2 : 0], i == 0 ? EnumFacing.NORTH : EnumFacing.SOUTH,
					ModelRotation.X0_Y0, null, true, true
				)
			);

			if (i == 0) {
				minX.setX(max);
				maxX.setX(max);
				minY.setY(max);
				maxY.setY(max);
				minZ.setZ(max);
				maxZ.setZ(max);
			}
		}

		for (EnumFacing f : EnumFacing.VALUES) {
			if (wc(wire, f)) {
				quads.add(
						faceBakery.makeBakedQuad(
								new Vector3f(min, 0.0F, min), new Vector3f(max, 0.0f, max),
								new BlockPartFace(f, -1, "", new BlockFaceUV(new float[] {min, min, max, max}, 0)),
								icons[lit ? 3 : 1], EnumFacing.DOWN, ROTATIONS[f.ordinal()], null, false, true
						)
				);
			}
		}
	}

	public void addWire(TileWire wire, TileWire.WireSide side, boolean lit, List<BakedQuad> quads) {
		if (side == TileWire.WireSide.FREESTANDING) {
			addWireFreestanding(wire, lit, quads);
			return;
		}

		float min = 8.0f - (width / 2);
		float max = 8.0f + (width / 2);
		float minH = 0.0f;
		float maxH = width;
		
		boolean[] connectionMatrix = new boolean[] {
				wire == null ? true : wire.connects(side, CONNECTION_DIRS[side.ordinal()][0]),
				wire == null ? true : wire.connects(side, CONNECTION_DIRS[side.ordinal()][1]),
				wire == null ? true : wire.connects(side, CONNECTION_DIRS[side.ordinal()][2]),
				wire == null ? true : wire.connects(side, CONNECTION_DIRS[side.ordinal()][3])
		};

		ModelRotation rot = ROTATIONS[side.ordinal()];

		// Center face

		Vector3f from = new Vector3f(min, width, min);
		Vector3f to = new Vector3f(max, width, max);

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

		quads.add(
				faceBakery.makeBakedQuad(
						from, to,
						new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {from.getX(), from.getZ(), to.getX(), to.getZ()}, 0)),
						icons[lit ? 2 : 0], EnumFacing.UP,
						rot, null, false, true
				)
		);

		from.setY(0.0F);
		to.setY(0.0F);
		quads.add(
				faceBakery.makeBakedQuad(
						from, to,
						new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {from.getX(), from.getZ(), to.getX(), to.getZ()}, 0)),
						icons[lit ? 2 : 0], EnumFacing.DOWN, rot, null, false, true
				)
		);

		// Side faces
		Vector3f fromZ = new Vector3f(connectionMatrix[2] ? 0.0f : min, 0.0f, min);
		Vector3f toZ = new Vector3f(connectionMatrix[3] ? 16.0f : max, 2.0f, min);
		Vector3f fromX = new Vector3f(min, 0.0f, connectionMatrix[0] ? 0.0f : min);
		Vector3f toX = new Vector3f(min, 2.0f, connectionMatrix[1] ? 16.0f : max);

		quads.add(
			faceBakery.makeBakedQuad(
				fromX, toX,
				new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()}, 0)),
				icons[lit ? 3 : 1], EnumFacing.WEST, rot, null, false, true
			)
		);

		quads.add(
			faceBakery.makeBakedQuad(
				fromZ, toZ,
				new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()}, 0)),
				icons[lit ? 3 : 1], EnumFacing.NORTH, rot, null, false, true
			)
		);

		fromX.setX(max);
		toX.setX(max);

		fromZ.setZ(max);
		toZ.setZ(max);

		quads.add(
			faceBakery.makeBakedQuad(
				fromX, toX,
				new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()}, 0)),
				icons[lit ? 3 : 1], EnumFacing.EAST, rot, null, false, true
			)
		);

		quads.add(
			faceBakery.makeBakedQuad(
				fromZ, toZ,
				new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()}, 0)),
				icons[lit ? 3 : 1], EnumFacing.SOUTH, rot, null, false, true
			)
		);

		// Edge faces

		if (connectionMatrix[0]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(min, minH, 0.0F), new Vector3f(max, maxH, 0.0F),
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {min, minH, max, maxH}, 0)),
					icons[lit ? 3 : 1], EnumFacing.NORTH, rot, null, false, true
				)
			);
		}

		if (connectionMatrix[1]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(min, minH, 16.0F), new Vector3f(max, maxH, 16.0F),
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {min, minH, max, maxH}, 0)),
					icons[lit ? 3 : 1], EnumFacing.SOUTH, rot, null, false, true
				)
			);
		}

		if (connectionMatrix[2]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(0.0F, minH, min), new Vector3f(0.0F, maxH, max),
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {min, minH, max, maxH}, 0)),
					icons[lit ? 3 : 1], EnumFacing.WEST, rot, null, false, true
				)
			);
		}

		if (connectionMatrix[3]) {
			quads.add(
				faceBakery.makeBakedQuad(
					new Vector3f(16.0F, minH, min), new Vector3f(16.0F, maxH, max),
					new BlockPartFace(null, -1, "", new BlockFaceUV(new float[] {min, minH, max, maxH}, 0)),
					icons[lit ? 3 : 1], EnumFacing.EAST, rot, null, false, true
				)
			);
		}
	}


	@Override
	public List<BakedQuad> getGeneralQuads() {
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		TileWire wire = null;
		if (state instanceof IExtendedBlockState) {
			wire = ((IExtendedBlockState) state).getValue(TileWire.PROPERTY);
		}

		if (wire != null) {
			for (TileWire.WireSide side : TileWire.WireSide.VALUES) {
				if (wire.hasWire(side)) {
					addWire(wire, side, wire.getSignalLevel() > 0, quads);
				}
			}
		} else if (stack != null) {
			if (ItemWire.isFreestanding(stack)) {
				addWireFreestanding(null, false, quads);
			} else {
				addWire(null, TileWire.WireSide.DOWN, false, quads);
			}
		}

		return quads;
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
