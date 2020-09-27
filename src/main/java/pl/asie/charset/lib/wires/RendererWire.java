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

package pl.asie.charset.lib.wires;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.SimpleBakedModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RendererWire extends ModelFactory<Wire> {
    private final ModelRotation[] ROTATIONS = new ModelRotation[]{
            ModelRotation.X0_Y0,
            ModelRotation.X180_Y0,
            ModelRotation.X270_Y0,
            ModelRotation.X270_Y180,
            ModelRotation.X270_Y270,
            ModelRotation.X270_Y90
    };

    private final Map<WireProvider, IWireRenderContainer> containerMap = new HashMap<>();

    public RendererWire() {
        super(Wire.PROPERTY, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    private <T> void addNonNull(Collection<T> coll, T object) {
    	if (object != null) {
    		coll.add(object);
	    }
    }

    public IWireRenderContainer getContainer(WireProvider type) {
        return containerMap.get(type);
    }

    private boolean wc(Wire wire, EnumFacing facing) {
        return wire.connects(facing);
    }

    private boolean isCenterEdge(Wire wire, WireFace side) {
        // TODO
        //return wire.getWireType(side) != wire.getWireType(WireFace.CENTER);
        return false;
    }

    private float getCL(WireRenderHandler handler, Wire wire, WireFace side) {
        // TODO
        //float h = wire != null && wire.hasWire(side) ? wire.getWireKind(side).height() : 0;
        float h = 0;

        if (wire != null && isCenterEdge(wire, side)) {
            h = 0;
        }

        if (!wc(wire, side.facing)) {
            h = 8.0f - (handler.getWidth() * 8);
        } else if (wire.getLocation() == WireFace.CENTER && side != WireFace.CENTER) {
        	WireNeighborWHCache cache = wire.getNeighborWHCache();
        	if (cache != null) {
        		float myWidth = handler.getWidth();
        		float otherWidth = cache.getWidth(side.facing);
        		if (otherWidth == myWidth) {
        			h = cache.getHeight(side.facing) * 16f;
		        }
	        }
        }

        return side.facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16.0f - h : h;
    }

    // NORTH SOUTH WEST EAST
    protected void addTopFaceCplxInner(WireRenderHandler handler, Wire wire, EnumFacing facing, EnumFacing renderFace, EnumFacing[] dirs, int dirI, Vector3f from, Vector3f to, List<BakedQuad> quads, int connMask, ModelRotation rot) {
    	float minX = 8.0f - (handler.getWidth() * 8);
    	float maxX = 16.0f - minX;
    	float minZ = minX;
    	float maxZ = maxX;

    	int dirP = dirI;
	    switch (facing) {
		    case UP:
		    	if (dirP < 2)
		    		dirP ^= 1;
			    break;
		    case SOUTH:
		    case EAST:
			    if (dirP >= 2)
				    dirP ^= 1;
		    	break;
	    }

	    switch (dirP) {
		    case 0:
			    minZ = 0.0f;
			    maxZ = minX;
			    break;
		    case 1:
			    minZ = maxZ;
			    maxZ = 16.0f;
			    break;
		    case 2:
			    minX = 0.0f;
			    maxX = minZ;
			    break;
		    case 3:
			    minX = maxX;
			    maxX = 16.0f;
			    break;
		    default:
		        break;
	    }

	    switch (renderFace.getAxis()) {
		    case X:
			    from = new Vector3f(from.x, minX, minZ);
			    to = new Vector3f(to.x, maxX, maxZ);
			    break;
		    case Y:
			    from = new Vector3f(minX, from.y, minZ);
			    to = new Vector3f(maxX, to.y, maxZ);
			    break;
		    case Z:
			    from = new Vector3f(minX, minZ, from.z);
			    to = new Vector3f(maxX, maxZ, to.z);
			    break;
	    }

	    addNonNull(quads,
			    CharsetFaceBakery.INSTANCE.makeBakedQuad(
					    from, to, handler.getColor(WireRenderHandler.TextureType.TOP, wire, dirI < 0 ? null : dirs[dirI]),
					    new float[] { minX, minZ, maxX, maxZ },
					    handler.getTexture(WireRenderHandler.TextureType.TOP, wire, dirI < 0 ? null : dirs[dirI], connMask),
					    renderFace, rot, true
			    )
	    );
    }

    protected void makeTopFace(List<BakedQuad> quads, WireRenderHandler handler, Wire wire, Vector3f from, Vector3f to, int connMask, EnumFacing facing, EnumFacing renderFacing, ModelRotation rot) {
    	if (!handler.isTopSimple()) {
    		// Render the top face as up to five quads
		    EnumFacing[] dirs = WireUtils.getConnectionsForRender(WireFace.get(EnumFacing.byIndex(facing.ordinal() & (~1))));
		    addTopFaceCplxInner(handler, wire, facing, renderFacing, dirs, -1, from, to, quads, connMask, rot);

		    switch (facing) {
			    case UP:
			    	connMask = (connMask & 3) | ((connMask & 8) >> 1) | ((connMask & 4) << 1);
			    	break;
			    case SOUTH:
			    case EAST:
				    connMask = (connMask & 12) | ((connMask & 2) >> 1) | ((connMask & 1) << 1);
				    break;
		    }

		    if ((connMask & 8) != 0) addTopFaceCplxInner(handler, wire, facing, renderFacing, dirs, 0, from, to, quads, connMask, rot);
		    if ((connMask & 4) != 0) addTopFaceCplxInner(handler, wire, facing, renderFacing, dirs, 1, from, to, quads, connMask, rot);
		    if ((connMask & 2) != 0) addTopFaceCplxInner(handler, wire, facing, renderFacing, dirs, 2, from, to, quads, connMask, rot);
		    if ((connMask & 1) != 0) addTopFaceCplxInner(handler, wire, facing, renderFacing, dirs, 3, from, to, quads, connMask, rot);
		    return;
	    }

	    addNonNull(quads,
			    CharsetFaceBakery.INSTANCE.makeBakedQuad(
					    from, to, handler.getColor(WireRenderHandler.TextureType.TOP, wire, null),
					    handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, connMask),
						renderFacing, rot, true
			    )
	    );
    }

    public void addWireFreestanding(WireRenderHandler handler, Wire wire, List<BakedQuad> quads) {
        float min = 8.0f - (handler.getWidth() * 8);
        float max = 16.0f - min;
        Vector3f minX = new Vector3f(min, getCL(handler, wire, WireFace.DOWN), getCL(handler, wire, WireFace.NORTH));
        Vector3f maxX = new Vector3f(min, getCL(handler, wire, WireFace.UP), getCL(handler, wire, WireFace.SOUTH));
        Vector3f minY = new Vector3f(getCL(handler, wire, WireFace.WEST), min, getCL(handler, wire, WireFace.NORTH));
        Vector3f maxY = new Vector3f(getCL(handler, wire, WireFace.EAST), min, getCL(handler, wire, WireFace.SOUTH));
        Vector3f minZ = new Vector3f(getCL(handler, wire, WireFace.WEST), getCL(handler, wire, WireFace.DOWN), min);
        Vector3f maxZ = new Vector3f(getCL(handler, wire, WireFace.EAST), getCL(handler, wire, WireFace.UP), min);

        int cmcX = (wc(wire, EnumFacing.UP) ? 8 : 0) | (wc(wire, EnumFacing.DOWN) ? 4 : 0) | (wc(wire, EnumFacing.NORTH) ? 2 : 0) | (wc(wire, EnumFacing.SOUTH) ? 1 : 0);
        int cmcY = (wc(wire, EnumFacing.NORTH) ? 4 : 0) | (wc(wire, EnumFacing.SOUTH) ? 8 : 0) | (wc(wire, EnumFacing.WEST) ? 2 : 0) | (wc(wire, EnumFacing.EAST) ? 1 : 0);
        int cmcZ = (wc(wire, EnumFacing.UP) ? 8 : 0) | (wc(wire, EnumFacing.DOWN) ? 4 : 0) | (wc(wire, EnumFacing.WEST) ? 1 : 0) | (wc(wire, EnumFacing.EAST) ? 2 : 0);

	    makeTopFace(quads, handler, wire, minX, maxX, cmcX, EnumFacing.WEST, EnumFacing.WEST, ModelRotation.X0_Y0);
	    makeTopFace(quads, handler, wire, minY, maxY, cmcY, EnumFacing.DOWN, EnumFacing.DOWN, ModelRotation.X0_Y0);
	    makeTopFace(quads, handler, wire, minZ, maxZ, cmcZ, EnumFacing.NORTH, EnumFacing.NORTH, ModelRotation.X0_Y0);

        // set to max
        minX.setX(max);
        maxX.setX(max);
        minY.setY(max);
        maxY.setY(max);
        minZ.setZ(max);
        maxZ.setZ(max);

        // swap
	    cmcY = (cmcY & 0x3) | ((cmcY & 0x8) >> 1) | ((cmcY & 0x4) << 1);
        cmcZ = (cmcZ & 0xC) | ((cmcZ & 0x2) >> 1) | ((cmcZ & 0x1) << 1);
        cmcX = (cmcX & 0xC) | ((cmcX & 0x2) >> 1) | ((cmcX & 0x1) << 1);

	    makeTopFace(quads, handler, wire, minX, maxX, cmcX, EnumFacing.EAST, EnumFacing.EAST, ModelRotation.X0_Y0);
	    makeTopFace(quads, handler, wire, minY, maxY, cmcY, EnumFacing.UP, EnumFacing.UP, ModelRotation.X0_Y0);
	    makeTopFace(quads, handler, wire, minZ, maxZ, cmcZ, EnumFacing.SOUTH, EnumFacing.SOUTH, ModelRotation.X0_Y0);

        for (EnumFacing f : EnumFacing.VALUES) {
            if (wc(wire, f)) {
                addNonNull(quads,
		                CharsetFaceBakery.INSTANCE.makeBakedQuad(
				                new Vector3f(min, 0.0F, min), new Vector3f(max, 0.0f, max),
				                handler.getColor(WireRenderHandler.TextureType.EDGE, wire, f),
				                f.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? new float[]{max, min, min, max} : new float[]{min, min, max, max},
				                handler.getTexture(WireRenderHandler.TextureType.EDGE, wire, f, 15),
				                EnumFacing.DOWN, ROTATIONS[f.ordinal()], true
		                )
                );
            }
        }
    }

    public void addCorner(WireRenderHandler handler, Wire wire, EnumFacing dir, List<BakedQuad> quads) {
        if (wire.getProvider().isFlat()) {
            return;
        }

        int width = (int) (handler.getWidth() * 16.0f);
        int height = (int) (handler.getHeight() * 16.0f);

        ModelRotation rot = ROTATIONS[wire.getLocation().ordinal()];
        float min = 8.0f - (width / 2f);
        float max = 16.0f - min;

        // Edge faces

	    float[] edgeUV = new float[]{16 - height, 16 - height, 16, 16};
        float[] edgeUVFlipped = new float[]{0, 16 - height, height, 16};

        if (dir == EnumFacing.NORTH) {
            float[] topUV = new float[]{min, 16 - height, max, 16};

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, 0, -height), new Vector3f(max, height, -height),
                            handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.NORTH), topUV,
		                    handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.NORTH, rot, false
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, height, -height), new Vector3f(max, height, 0),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.UP), topUV,
		                    handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.UP, rot, false
                    )
            );

	        addNonNull(quads,
			        CharsetFaceBakery.INSTANCE.makeBakedQuad(
					        new Vector3f(min, 0, -height), new Vector3f(max, 0, 0),
					        handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.DOWN), topUV,
					        handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.DOWN, rot, false
			        )
	        );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, 0, -height), new Vector3f(min, height, 0),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUV,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.WEST, rot, true
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(max, 0, -height), new Vector3f(max, height, 0),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUVFlipped,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.EAST, rot, true
                    )
            );
        } else if (dir == EnumFacing.SOUTH) {
            float[] topUV = new float[]{min, 0, max, height};

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, 0, 16 + height), new Vector3f(max, height, 16 + height),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.SOUTH), topUV,
                            handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.SOUTH, rot, false
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, height, 16), new Vector3f(max, height, 16 + height),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.UP), topUV,
                            handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.UP, rot, false
                    )
            );

	        addNonNull(quads,
			        CharsetFaceBakery.INSTANCE.makeBakedQuad(
					        new Vector3f(min, 0, 16), new Vector3f(max, 0, 16 + height),
					        handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.DOWN), topUV,
					        handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.DOWN, rot, false
			        )
	        );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, 0, 16), new Vector3f(min, height, 16 + height),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUVFlipped,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.WEST, rot, true
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(max, 0, 16), new Vector3f(max, height, 16 + height),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUV,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.EAST, rot, true
                    )
            );
        } else if (dir == EnumFacing.WEST) {
            float[] topUV = new float[]{16 - height, min, 16, max};
	        float[] topUV2 = new float[]{min, 16 - height, max, 16};

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(-height, height, min), new Vector3f(0, height, max),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.UP), topUV,
                            handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.UP, rot, false
                    )
            );

	        addNonNull(quads,
			        CharsetFaceBakery.INSTANCE.makeBakedQuad(
					        new Vector3f(-height, 0, min), new Vector3f(0, 0, max),
					        handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.DOWN), topUV,
					        handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.DOWN, rot, false
			        )
	        );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(-height, 0, min), new Vector3f(-height, height, max),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.WEST), topUV2,
                            handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.WEST, rot, false
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(-height, 0, min), new Vector3f(0, height, min),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUVFlipped,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.NORTH, rot, true
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(-height, 0, max), new Vector3f(0, height, max),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUV,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.SOUTH, rot, true
                    )
            );
        } else if (dir == EnumFacing.EAST) {
            float[] topUV = new float[]{0, min, height, max};
	        float[] topUV2 = new float[]{min, 0, max, height};

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(16, height, min), new Vector3f(16 + height, height, max),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.UP), topUV,
                            handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.UP, rot, false
                    )
            );

	        addNonNull(quads,
			        CharsetFaceBakery.INSTANCE.makeBakedQuad(
					        new Vector3f(16, 0, min), new Vector3f(16 + height, 0, max),
					        handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.DOWN), topUV,
					        handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.DOWN, rot, false
			        )
	        );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(16 + height, 0, min), new Vector3f(16 + height, height, max),
		                    handler.getColor(WireRenderHandler.TextureType.TOP, wire, EnumFacing.EAST), topUV2,
                            handler.getTexture(WireRenderHandler.TextureType.TOP, wire, null, 15), EnumFacing.EAST, rot, false
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(16, 0, min), new Vector3f(16 + height, height, min),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUV,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.NORTH, rot, true
                    )
            );

            addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(16, 0, max), new Vector3f(16 + height, height, max),
                            handler.getColor(WireRenderHandler.TextureType.CORNER, wire, null), edgeUVFlipped,
                            handler.getTexture(WireRenderHandler.TextureType.CORNER, wire, null, 15), EnumFacing.SOUTH, rot, true
                    )
            );
        }
    }

    public void addWire(WireRenderHandler handler, Wire wire, List<BakedQuad> quads) {
	    WireNeighborWHCache cache = wire.getNeighborWHCache();
        WireFace side = wire.getLocation();

        if (side == WireFace.CENTER) {
            addWireFreestanding(handler, wire, quads);
            return;
        }

        float min = 8.0f - (handler.getWidth() * 8f);
        float max = 16.0f - min;
        float minH = 0.0f;
        float maxH = handler.getHeight() * 16f;
        EnumFacing[] dirs = WireUtils.getConnectionsForRender(side);

        boolean[] connectionMatrix = new boolean[]{
                wire.connectsAny(dirs[0]),
                wire.connectsAny(dirs[1]),
		        wire.connectsAny(dirs[2]),
		        wire.connectsAny(dirs[3])
        };
        int cmc = (connectionMatrix[0] ? 8 : 0) | (connectionMatrix[1] ? 4 : 0) | (connectionMatrix[2] ? 2 : 0) | (connectionMatrix[3] ? 1 : 0);

        boolean[] cornerConnectionMatrix = new boolean[]{
		        wire.connectsCorner(dirs[0]),
		        wire.connectsCorner(dirs[1]),
		        wire.connectsCorner(dirs[2]),
		        wire.connectsCorner(dirs[3])
        };

        ModelRotation rot = ROTATIONS[side.ordinal()];

        // Center face

        Vector3f from = new Vector3f(min, maxH, min);
        Vector3f to = new Vector3f(max, maxH, max);

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

        makeTopFace(quads, handler, wire, from, to, cmc, wire.getLocation().facing, EnumFacing.UP, rot);

        if (!wire.getProvider().isFlat()) {
	        // If I'm the non-authoritative side...
	        if (cache != null) {
		        for (int i = 0; i < 4; i++) {
			        if (wire.connectsInternal(dirs[i]) && wire.getLocation().ordinal() < dirs[i].ordinal()) {
				        if (cache.getWidth(dirs[i]) == wire.getProvider().getWidth()) {
					        switch (i) {
						        case 0:
							        from.setZ(cache.getHeight(dirs[i]) * 16);
							        break;
						        case 1:
							        to.setZ(16.0f - cache.getHeight(dirs[i]) * 16);
							        break;
						        case 2:
							        from.setX(cache.getHeight(dirs[i]) * 16);
							        break;
						        case 3:
							        to.setX(16.0f - cache.getHeight(dirs[i]) * 16);
							        break;
					        }
				        }
			        }
		        }
	        }

	        from.setY(0.0F);
            to.setY(0.0F);
	        makeTopFace(quads, handler, wire, from, to, cmc, wire.getLocation().facing, EnumFacing.DOWN, rot);

            // Side faces
            Vector3f fromZ = new Vector3f(from.getX(), 0.0f, min);
            Vector3f toZ = new Vector3f(to.getX(), maxH, min);
            Vector3f fromX = new Vector3f(min, 0.0f, from.getZ());
            Vector3f toX = new Vector3f(min, maxH, to.getZ());

            // Should we render a faux side wire on this side? (For bundled)
            boolean crossroadsX = connectionMatrix[2] && !connectionMatrix[3];
            boolean crossroadsZ = connectionMatrix[0] && !connectionMatrix[1];

            boolean renderSideX = connectionMatrix[0] || connectionMatrix[1];
	        boolean renderSideZ = connectionMatrix[2] || connectionMatrix[3];

            // getIcon(false, cmc == 1, crossroadsX, EnumFacing.WEST)
	        if (!connectionMatrix[2] || renderSideX) addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            fromX, toX,
                            handler.getColor(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.WEST), new float[]{fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()},
		                    handler.getTexture(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.WEST, cmc), EnumFacing.WEST, rot, false
                    )
            );

            // getIcon(false, cmc == 0 || cmc == 4, crossroadsZ, EnumFacing.NORTH)
	        if (!connectionMatrix[0] || renderSideZ) addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            fromZ, toZ,
		                    handler.getColor(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.NORTH), new float[]{toZ.getX(), fromZ.getY(), fromZ.getX(), toZ.getY()},
		                    handler.getTexture(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.NORTH, cmc), EnumFacing.NORTH, rot, false
                    )
            );

            fromX.setX(max);
            toX.setX(max);

            fromZ.setZ(max);
            toZ.setZ(max);

            // getIcon(false, cmc == 2, crossroadsX, EnumFacing.EAST)
	        if (!connectionMatrix[3] || renderSideX) addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            fromX, toX,
		                    handler.getColor(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.EAST), new float[]{toX.getZ(), fromX.getY(), fromX.getZ(), toX.getY()},
                            handler.getTexture(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.EAST, cmc), EnumFacing.EAST, rot, false
                    )
            );

            // getIcon(false, cmc == 0 || cmc == 8, crossroadsZ, EnumFacing.SOUTH)
	        if (!connectionMatrix[1] || renderSideZ) addNonNull(quads,
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            fromZ, toZ,
		                    handler.getColor(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.SOUTH), new float[]{fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()},
		                    handler.getTexture(WireRenderHandler.TextureType.SIDE, wire, EnumFacing.SOUTH, cmc), EnumFacing.SOUTH, rot, false
                    )
            );

            // Edge faces
            float[] edgeUV = new float[]{min, minH, max, maxH};
            float[] edgeUVFlipped = new float[]{max, minH, min, maxH};

            if (connectionMatrix[0]) {
                addNonNull(quads,
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(min, minH, 0.0F), new Vector3f(max, maxH, 0.0F),
                                handler.getColor(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.NORTH), edgeUVFlipped,
                                handler.getTexture(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.NORTH, 15), EnumFacing.NORTH, rot, false
                        )
                );
            }

            if (connectionMatrix[1]) {
                addNonNull(quads,
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(min, minH, 16.0F), new Vector3f(max, maxH, 16.0F),
                                handler.getColor(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.SOUTH), edgeUV,
		                        handler.getTexture(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.SOUTH, 15), EnumFacing.SOUTH, rot, false
                        )
                );
            }

            if (connectionMatrix[2]) {
                addNonNull(quads,
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(0.0F, minH, min), new Vector3f(0.0F, maxH, max),
                                handler.getColor(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.WEST), edgeUV,
		                        handler.getTexture(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.WEST, 15), EnumFacing.WEST, rot, false
                        )
                );
            }

            if (connectionMatrix[3]) {
                addNonNull(quads,
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(16.0F, minH, min), new Vector3f(16.0F, maxH, max),
                                handler.getColor(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.EAST), edgeUVFlipped,
		                        handler.getTexture(WireRenderHandler.TextureType.EDGE, wire, EnumFacing.EAST, 15), EnumFacing.EAST, rot, false
                        )
                );
            }

            EnumFacing[] dirs0 = WireUtils.getConnectionsForRender(WireFace.DOWN);
            for (int i = 0; i < 4; i++) {
                if (cornerConnectionMatrix[i]) {
                    addCorner(handler, wire, dirs0[i], quads);
                }
            }
        }
    }

	protected IBakedModel bakeWire(Wire wire, boolean isDynamic, boolean isItem, BlockRenderLayer blockLayer) {
    	boolean hasQuads = false;

		SimpleBakedModel model = new SimpleBakedModel(this);
		if (wire != null) {
			IWireRenderContainer container = containerMap.get(wire.getProvider());
			if (container != null) {
				for (int i = 0; i < container.getLayerCount(); i++) {
					WireRenderHandler handler = container.get(i);
					if (i == 0) {
						model.setParticle(handler.getTexture(WireRenderHandler.TextureType.PARTICLE, wire,null, 15));
					}

                    boolean shouldRender = isItem;
                    if (!shouldRender) {
                        boolean handlerIsDynamic = handler.isDynamic();
                        if (handlerIsDynamic) {
                            shouldRender = isDynamic;
                        } else {
                            shouldRender = !isDynamic && blockLayer == handler.getRenderLayer();
                        }
                    }

                    if (shouldRender) {
						hasQuads = true;
						addWire(handler, wire, model.getQuads(null, null, 0));
					}
				}
			}
		}

		return (!isItem && isDynamic && !hasQuads) ? null : model;
	}

    @Override
    public IBakedModel bake(Wire wire, boolean isItem, BlockRenderLayer blockLayer) {
    	return bakeWire(wire, false, isItem, blockLayer);
    }

    @Override
    public Wire fromItemStack(ItemStack stack) {
        Wire wire = ((ItemWire) stack.getItem()).fromStack(new IWireContainer.Dummy(), stack, EnumFacing.DOWN);
        if (wire != null) {
            wire.setConnectionsForItemRender();
        }
        return wire;
    }

	public void registerContainer(WireProvider provider, IWireRenderContainer container) {
    	containerMap.put(provider, container);
	}

	protected void reloadTextures(TextureMap map) {
    	for (IWireRenderContainer container : containerMap.values()) {
    		for (int i = 0; i < container.getLayerCount(); i++) {
    			WireRenderHandler handler = container.get(i);
    			handler.refresh(map);
		    }
	    }
	}
}
