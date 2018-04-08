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

package pl.asie.charset.lib.wires;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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
import pl.asie.charset.lib.render.sprite.SpritesheetFactory;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RendererWire extends ModelFactory<Wire> {
    public static class WireSheet {
        @Nonnull final TextureAtlasSprite[] top;
        @Nullable final TextureAtlasSprite side;
        @Nullable final TextureAtlasSprite edge;
        @Nonnull final TextureAtlasSprite particle;
        final int width, height;

        private WireSheet(TextureMap map, String domain, String path, WireProvider type) {
            top = SpritesheetFactory.register(map, new ResourceLocation(domain, path + "top"), 4, 4);
            particle = map.registerSprite(new ResourceLocation(domain, path + "particle"));
            if (!type.isFlat()) {
                edge = map.registerSprite(new ResourceLocation(domain, path + "edge"));
                side = map.registerSprite(new ResourceLocation(domain, path + "side"));
            } else {
                edge = null;
                side = null;
            }

            width = (int) (type.getWidth() * 16);
            height = (int) (type.getHeight() * 16);
        }
    }

    private final ModelRotation[] ROTATIONS = new ModelRotation[]{
            ModelRotation.X0_Y0,
            ModelRotation.X180_Y0,
            ModelRotation.X270_Y0,
            ModelRotation.X270_Y180,
            ModelRotation.X270_Y270,
            ModelRotation.X270_Y90
    };

    private final TIntObjectMap<Wire> stackMap = new TIntObjectHashMap<>();
    private final Map<WireProvider, WireSheet> sheetMap = new HashMap<>();

    public RendererWire() {
        super(Wire.PROPERTY, new ResourceLocation("minecraft:blocks/stone"));
        addDefaultBlockTransforms();
    }

    public WireSheet getSheet(WireProvider type) {
        return sheetMap.get(type);
    }

    public void registerSheet(TextureMap map, WireProvider type) {
        ResourceLocation location = type.getTexturePrefix();
        String domain = location.getResourceDomain();
        String path = location.getResourcePath();

        if (!path.endsWith("/")) {
            path += "_";
        }

        WireSheet sheet = new WireSheet(map, domain, path, type);
        sheetMap.put(type, sheet);
    }

    private boolean wc(Wire wire, EnumFacing facing) {
        return wire.connects(facing);
    }

    private boolean isCenterEdge(Wire wire, WireFace side) {
        // TODO
        //return wire.getWireType(side) != wire.getWireType(WireFace.CENTER);
        return false;
    }

    private float getCL(Wire wire, WireFace side) {
        // TODO
        //float h = wire != null && wire.hasWire(side) ? wire.getWireKind(side).height() : 0;
        float h = 0;

        if (wire != null && isCenterEdge(wire, side)) {
            h = 0;
        }

        if (!wc(wire, side.facing) && wire.getFactory() != null) {
            h = 8.0f - (sheetMap.get(wire.getFactory()).width / 2); // TODO: Replace with WireProvider call?
        }

        return side.facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16.0f - h : h;
    }

    public void addWireFreestanding(Wire wire, WireSheet sheet, int renderColor, List<BakedQuad> quads) {
        float min = 8.0f - (sheet.width / 2);
        float max = 8.0f + (sheet.width / 2);
        Vector3f minX = new Vector3f(min, getCL(wire, WireFace.DOWN), getCL(wire, WireFace.NORTH));
        Vector3f maxX = new Vector3f(min, getCL(wire, WireFace.UP), getCL(wire, WireFace.SOUTH));
        Vector3f minY = new Vector3f(getCL(wire, WireFace.WEST), min, getCL(wire, WireFace.NORTH));
        Vector3f maxY = new Vector3f(getCL(wire, WireFace.EAST), min, getCL(wire, WireFace.SOUTH));
        Vector3f minZ = new Vector3f(getCL(wire, WireFace.WEST), getCL(wire, WireFace.DOWN), min);
        Vector3f maxZ = new Vector3f(getCL(wire, WireFace.EAST), getCL(wire, WireFace.UP), min);

        int cmcX = (wc(wire, EnumFacing.UP) ? 8 : 0) | (wc(wire, EnumFacing.DOWN) ? 4 : 0) | (wc(wire, EnumFacing.NORTH) ? 2 : 0) | (wc(wire, EnumFacing.SOUTH) ? 1 : 0);
        int cmcY = (wc(wire, EnumFacing.NORTH) ? 4 : 0) | (wc(wire, EnumFacing.SOUTH) ? 8 : 0) | (wc(wire, EnumFacing.WEST) ? 2 : 0) | (wc(wire, EnumFacing.EAST) ? 1 : 0);
        int cmcZ = (wc(wire, EnumFacing.UP) ? 8 : 0) | (wc(wire, EnumFacing.DOWN) ? 4 : 0) | (wc(wire, EnumFacing.WEST) ? 1 : 0) | (wc(wire, EnumFacing.EAST) ? 2 : 0);

        for (int i = 0; i < 2; i++) {
            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            minX, maxX, renderColor,
                            sheet.top[cmcX], i == 0 ? EnumFacing.WEST : EnumFacing.EAST,
                            ModelRotation.X0_Y0, true
                    )
            );

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            minY, maxY, renderColor,
                            sheet.top[cmcY], i == 0 ? EnumFacing.DOWN : EnumFacing.UP,
                            ModelRotation.X0_Y0, true
                    )
            );

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            minZ, maxZ, renderColor,
                            sheet.top[cmcZ], i == 0 ? EnumFacing.NORTH : EnumFacing.SOUTH,
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
                cmcZ = (cmcZ & 0xC) | ((cmcZ & 0x2) >> 1) | ((cmcZ & 0x1) << 1);
                cmcX = (cmcX & 0xC) | ((cmcX & 0x2) >> 1) | ((cmcX & 0x1) << 1);
            }
        }

        if (sheet.edge != null) {
            for (EnumFacing f : EnumFacing.VALUES) {
                if (wc(wire, f)) {
                    quads.add(
                            CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                    new Vector3f(min, 0.0F, min), new Vector3f(max, 0.0f, max),
                                    renderColor,
                                    f.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? new float[]{max, min, min, max} : new float[]{min, min, max, max},
                                    sheet.edge, EnumFacing.DOWN, ROTATIONS[f.ordinal()], true
                            )
                    );
                }
            }
        }
    }

    public void addCorner(Wire wire, WireSheet sheet, EnumFacing dir, int renderColor, List<BakedQuad> quads) {
        if (wire.getFactory().isFlat()) {
            return;
        }

        int width = sheet.width;
        int height = sheet.height;

        ModelRotation rot = ROTATIONS[wire.getLocation().ordinal()];
        float min = 8.0f - (width / 2);
        float max = 8.0f + (width / 2);

        // Edge faces

        float[] edgeUV = new float[]{min, 0, max, height};
        float[] edgeUVFlipped = new float[]{max, 0, min, height};

        if (dir == EnumFacing.NORTH) {
            float[] topUV = new float[]{min, 16 - height, max, 16};

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, 0, -height), new Vector3f(max, height, -height),
                            renderColor, edgeUVFlipped,
                            sheet.top[15], EnumFacing.NORTH, rot, false
                    )
            );

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, height, -height), new Vector3f(max, height, 0),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            if (sheet.edge != null) {
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(min, 0, -height), new Vector3f(min, height, 0),
                                renderColor, edgeUV,
                                sheet.edge, EnumFacing.WEST, rot, false
                        )
                );

                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(max, 0, -height), new Vector3f(max, height, 0),
                                renderColor, edgeUVFlipped,
                                sheet.edge, EnumFacing.EAST, rot, false
                        )
                );
            }
        } else if (dir == EnumFacing.SOUTH) {
            float[] topUV = new float[]{min, 0, max, height};

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, 0, 16 + height), new Vector3f(max, height, 16 + height),
                            renderColor, edgeUVFlipped,
                            sheet.top[15], EnumFacing.SOUTH, rot, false
                    )
            );

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(min, height, 16), new Vector3f(max, height, 16 + height),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            if (sheet.edge != null) {
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(min, 0, 16), new Vector3f(min, height, 16 + height),
                                renderColor, edgeUV,
                                sheet.edge, EnumFacing.WEST, rot, false
                        )
                );

                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(max, 0, 16), new Vector3f(max, height, 16 + height),
                                renderColor, edgeUVFlipped,
                                sheet.edge, EnumFacing.EAST, rot, false
                        )
                );
            }
        } else if (dir == EnumFacing.WEST) {
            float[] topUV = new float[]{16 - height, min, 16, max};

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(-height, height, min), new Vector3f(0, height, max),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(-height, 0, min), new Vector3f(-height, height, max),
                            renderColor, edgeUV,
                            sheet.top[15], EnumFacing.WEST, rot, false
                    )
            );

            if (sheet.edge != null) {
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(-height, 0, min), new Vector3f(0, height, min),
                                renderColor, edgeUVFlipped,
                                sheet.edge, EnumFacing.NORTH, rot, false
                        )
                );

                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(-height, 0, max), new Vector3f(0, height, max),
                                renderColor, edgeUV,
                                sheet.edge, EnumFacing.SOUTH, rot, false
                        )
                );
            }
        } else if (dir == EnumFacing.EAST) {
            float[] topUV = new float[]{0, min, height, max};

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(16, height, min), new Vector3f(16 + height, height, max),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            new Vector3f(16 + height, 0, min), new Vector3f(16 + height, height, max),
                            renderColor, edgeUV,
                            sheet.top[15], EnumFacing.EAST, rot, false
                    )
            );

            if (sheet.edge != null) {
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(16, 0, min), new Vector3f(16 + height, height, min),
                                renderColor, edgeUVFlipped,
                                sheet.edge, EnumFacing.NORTH, rot, false
                        )
                );

                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                new Vector3f(16, 0, max), new Vector3f(16 + height, height, max),
                                renderColor, edgeUV,
                                sheet.edge, EnumFacing.SOUTH, rot, false
                        )
                );
            }
        }
    }

    public void addWire(Wire wire, WireSheet sheet, List<BakedQuad> quads) {
        WireFace side = wire.getLocation();
        int renderColor = wire.getRenderColor();

        if (side == WireFace.CENTER) {
            addWireFreestanding(wire, sheet, renderColor, quads);
            return;
        }

        float min = 8.0f - (sheet.width / 2);
        float max = 8.0f + (sheet.width / 2);
        float minH = 0.0f;
        float maxH = sheet.height;
        EnumFacing[] dirs = WireUtils.getConnectionsForRender(side);

        boolean[] connectionMatrix = new boolean[]{
                wire == null || wire.connectsAny(dirs[0]),
                wire == null || wire.connectsAny(dirs[1]),
                wire == null || wire.connectsAny(dirs[2]),
                wire == null || wire.connectsAny(dirs[3])
        };
        int cmc = (connectionMatrix[0] ? 8 : 0) | (connectionMatrix[1] ? 4 : 0) | (connectionMatrix[2] ? 2 : 0) | (connectionMatrix[3] ? 1 : 0);

        boolean[] cornerConnectionMatrix = new boolean[]{
                wire == null || wire.connectsCorner(dirs[0]),
                wire == null || wire.connectsCorner(dirs[1]),
                wire == null || wire.connectsCorner(dirs[2]),
                wire == null || wire.connectsCorner(dirs[3])
        };

        ModelRotation rot = ROTATIONS[side.ordinal()];

        // Center face

        Vector3f from = new Vector3f(min, sheet.height, min);
        Vector3f to = new Vector3f(max, sheet.height, max);

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
                CharsetFaceBakery.INSTANCE.makeBakedQuad(
                        from, to,
                        renderColor, new float[]{from.getX(), from.getZ(), to.getX(), to.getZ()},
                        sheet.top[cmc], EnumFacing.UP,
                        rot, true
                )
        );

        if (!wire.getFactory().isFlat()) {
            from.setY(0.0F);
            to.setY(0.0F);
            quads.add(
                    CharsetFaceBakery.INSTANCE.makeBakedQuad(
                            from, to,
                            renderColor, new float[]{from.getX(), from.getZ(), to.getX(), to.getZ()},
                            sheet.top[cmc], EnumFacing.DOWN, rot, true
                    )
            );


            // Side faces
            Vector3f fromZ = new Vector3f(from.getX(), 0.0f, min);
            Vector3f toZ = new Vector3f(to.getX(), sheet.height, min);
            Vector3f fromX = new Vector3f(min, 0.0f, from.getZ());
            Vector3f toX = new Vector3f(min, sheet.height, to.getZ());

            // Should we render a faux side wire on this side? (For bundled)
            boolean crossroadsX = connectionMatrix[2] && !connectionMatrix[3];
            boolean crossroadsZ = connectionMatrix[0] && !connectionMatrix[1];

            // getIcon(false, cmc == 1, crossroadsX, EnumFacing.WEST)
            if (sheet.side != null) {
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                fromX, toX,
                                renderColor, new float[]{fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()},
                                sheet.side, EnumFacing.WEST, rot, false
                        )
                );

                // getIcon(false, cmc == 0 || cmc == 4, crossroadsZ, EnumFacing.NORTH)
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                fromZ, toZ,
                                renderColor, new float[]{toZ.getX(), fromZ.getY(), fromZ.getX(), toZ.getY()},
                                sheet.side, EnumFacing.NORTH, rot, false
                        )
                );
            }

            fromX.setX(max);
            toX.setX(max);

            fromZ.setZ(max);
            toZ.setZ(max);

            if (sheet.side != null) {
                // getIcon(false, cmc == 2, crossroadsX, EnumFacing.EAST)
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                fromX, toX,
                                renderColor, new float[]{toX.getZ(), fromX.getY(), fromX.getZ(), toX.getY()},
                                sheet.side, EnumFacing.EAST, rot, false
                        )
                );

                // getIcon(false, cmc == 0 || cmc == 8, crossroadsZ, EnumFacing.SOUTH)
                quads.add(
                        CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                fromZ, toZ,
                                renderColor, new float[]{fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()},
                                sheet.side, EnumFacing.SOUTH, rot, false
                        )
                );
            }

            // Edge faces
            float[] edgeUV = new float[]{min, minH, max, maxH};
            float[] edgeUVFlipped = new float[]{max, minH, min, maxH};

            if (sheet.edge != null) {
                if (connectionMatrix[0]) {
                    quads.add(
                            CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                    new Vector3f(min, minH, 0.0F), new Vector3f(max, maxH, 0.0F),
                                    renderColor, edgeUVFlipped,
                                    sheet.edge, EnumFacing.NORTH, rot, false
                            )
                    );
                }

                if (connectionMatrix[1]) {
                    quads.add(
                            CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                    new Vector3f(min, minH, 16.0F), new Vector3f(max, maxH, 16.0F),
                                    renderColor, edgeUV,
                                    sheet.edge, EnumFacing.SOUTH, rot, false
                            )
                    );
                }

                if (connectionMatrix[2]) {
                    quads.add(
                            CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                    new Vector3f(0.0F, minH, min), new Vector3f(0.0F, maxH, max),
                                    renderColor, edgeUV,
                                    sheet.edge, EnumFacing.WEST, rot, false
                            )
                    );
                }

                if (connectionMatrix[3]) {
                    quads.add(
                            CharsetFaceBakery.INSTANCE.makeBakedQuad(
                                    new Vector3f(16.0F, minH, min), new Vector3f(16.0F, maxH, max),
                                    renderColor, edgeUVFlipped,
                                    sheet.edge, EnumFacing.EAST, rot, false
                            )
                    );
                }
            }

            EnumFacing[] dirs0 = WireUtils.getConnectionsForRender(WireFace.DOWN);
            for (int i = 0; i < 4; i++) {
                if (cornerConnectionMatrix[i]) {
                    addCorner(wire, sheet, dirs0[i], renderColor, quads);
                }
            }
        }
    }

    @Override
    public IBakedModel bake(Wire wire, boolean isItem, BlockRenderLayer layer) {
        SimpleBakedModel model = new SimpleBakedModel(this);
        if (wire != null) {
            WireSheet sheet = sheetMap.get(wire.getFactory());
            if (sheet != null) {
                model.setParticle(sheet.particle);
                addWire(wire, sheet, model.getQuads(null, null, 0));
            }
        }

        return model;
    }

    @Override
    public Wire fromItemStack(ItemStack stack) {
        int md = stack.getItemDamage();
        if (stackMap.containsKey(md)) {
            return stackMap.get(md);
        } else {
            Wire wire = CharsetLibWires.itemWire.fromStack(new IWireContainer.Dummy(), stack, EnumFacing.DOWN);
            if (wire != null) {
                wire.setConnectionsForItemRender();

                stackMap.put(md, wire);
                return wire;
            } else {
                return null;
            }
        }
    }
}
