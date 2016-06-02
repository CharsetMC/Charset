/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.wires.render;

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
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.render.ModelFactory;
import pl.asie.charset.lib.render.SimpleBakedModel;
import pl.asie.charset.lib.render.SpritesheetFactory;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.wires.ItemWire;
import pl.asie.charset.wires.WireUtils;
import pl.asie.charset.wires.logic.PartWireBase;
import pl.asie.charset.wires.logic.PartWireProvider;

import java.util.EnumMap;
import java.util.List;

public class RendererWire extends ModelFactory<PartWireBase> {
    private static class WireSheet {
        TextureAtlasSprite[] top;
        TextureAtlasSprite side, edge, particle;
        int width, height;
    }

    private final ModelRotation[] ROTATIONS = new ModelRotation[]{
            ModelRotation.X0_Y0,
            ModelRotation.X180_Y0,
            ModelRotation.X270_Y0,
            ModelRotation.X270_Y180,
            ModelRotation.X270_Y270,
            ModelRotation.X270_Y90
    };

    private final TIntObjectMap<PartWireBase> stackMap = new TIntObjectHashMap<>();
    private final EnumMap<WireType, WireSheet> sheetMap = new EnumMap<>(WireType.class);

    public RendererWire() {
        super(PartWireBase.PROPERTY, new ResourceLocation("charsetwires:blocks/normal_cross"));
        addDefaultBlockTransforms();
    }

    public void registerSheet(TextureMap map, WireType type, ResourceLocation location) {
        String domain = location.getResourceDomain();
        String path = location.getResourcePath();
        WireSheet sheet = new WireSheet();

        sheet.top = SpritesheetFactory.register(map, new ResourceLocation(domain, path + "_top"), 4, 4);
        sheet.particle = map.registerSprite(new ResourceLocation(domain, path + "_particle"));
        sheet.edge = map.registerSprite(new ResourceLocation(domain, path + "_edge"));
        sheet.side = map.registerSprite(new ResourceLocation(domain, path + "_side"));

        sheet.width = WireUtils.width(type);
        sheet.height = WireUtils.height(type);

        sheetMap.put(type, sheet);
    }

    private boolean wc(PartWireBase wire, EnumFacing facing) {
        return wire.connects(facing);
    }

    private boolean isCenterEdge(PartWireBase wire, WireFace side) {
        // TODO
        //return wire.getWireType(side) != wire.getWireType(WireFace.CENTER);
        return false;
    }

    private float getCL(PartWireBase wire, WireFace side) {
        // TODO
        //float h = wire != null && wire.hasWire(side) ? wire.getWireKind(side).height() : 0;
        float h = 0;

        if (wire != null && isCenterEdge(wire, side)) {
            h = 0;
        }

        if (!wc(wire, side.facing)) {
            h = 8.0f - (wire.type.width() / 2);
        }

        return side.facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16.0f - h : h;
    }

    public void addWireFreestanding(PartWireBase wire, WireSheet sheet, int renderColor, List<BakedQuad> quads) {
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
                    RenderUtils.BAKERY.makeBakedQuad(
                            minX, maxX, renderColor,
                            sheet.top[cmcX], i == 0 ? EnumFacing.WEST : EnumFacing.EAST,
                            ModelRotation.X0_Y0, true
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            minY, maxY, renderColor,
                            sheet.top[cmcY], i == 0 ? EnumFacing.DOWN : EnumFacing.UP,
                            ModelRotation.X0_Y0, true
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
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

        for (EnumFacing f : EnumFacing.VALUES) {
            if (wc(wire, f)) {
                quads.add(
                        RenderUtils.BAKERY.makeBakedQuad(
                                new Vector3f(min, 0.0F, min), new Vector3f(max, 0.0f, max),
                                renderColor,
                                f.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? new float[]{max, min, min, max} : new float[]{min, min, max, max},
                                sheet.edge, EnumFacing.DOWN, ROTATIONS[f.ordinal()], true
                        )
                );
            }
        }
    }

    public void addCorner(PartWireBase wire, WireSheet sheet, EnumFacing dir, int renderColor, List<BakedQuad> quads) {
        int width = sheet.width;
        int height = sheet.height;

        ModelRotation rot = ROTATIONS[wire.location.ordinal()];
        float min = 8.0f - (width / 2);
        float max = 8.0f + (width / 2);

        // Edge faces

        float[] edgeUV = new float[]{min, 0, max, height};
        float[] edgeUVFlipped = new float[]{max, 0, min, height};

        if (dir == EnumFacing.NORTH) {
            float[] topUV = new float[]{min, 16 - height, max, 16};

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, 0, -height), new Vector3f(max, height, -height),
                            renderColor, edgeUVFlipped,
                            sheet.top[15], EnumFacing.NORTH, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, height, -height), new Vector3f(max, height, 0),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, 0, -height), new Vector3f(min, height, 0),
                            renderColor, edgeUV,
                            sheet.edge, EnumFacing.WEST, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(max, 0, -height), new Vector3f(max, height, 0),
                            renderColor, edgeUVFlipped,
                            sheet.edge, EnumFacing.EAST, rot, false
                    )
            );
        } else if (dir == EnumFacing.SOUTH) {
            float[] topUV = new float[]{min, 0, max, height};

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, 0, 16 + height), new Vector3f(max, height, 16 + height),
                            renderColor, edgeUVFlipped,
                            sheet.top[15], EnumFacing.SOUTH, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, height, 16), new Vector3f(max, height, 16 + height),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, 0, 16), new Vector3f(min, height, 16 + height),
                            renderColor, edgeUV,
                            sheet.edge, EnumFacing.WEST, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(max, 0, 16), new Vector3f(max, height, 16 + height),
                            renderColor, edgeUVFlipped,
                            sheet.edge, EnumFacing.EAST, rot, false
                    )
            );
        } else if (dir == EnumFacing.WEST) {
            float[] topUV = new float[]{16 - height, min, 16, max};

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(-height, height, min), new Vector3f(0, height, max),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(-height, 0, min), new Vector3f(-height, height, max),
                            renderColor, edgeUV,
                            sheet.top[15], EnumFacing.WEST, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(-height, 0, min), new Vector3f(0, height, min),
                            renderColor, edgeUVFlipped,
                            sheet.edge, EnumFacing.NORTH, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(-height, 0, max), new Vector3f(0, height, max),
                            renderColor, edgeUV,
                            sheet.edge, EnumFacing.SOUTH, rot, false
                    )
            );
        } else if (dir == EnumFacing.EAST) {
            float[] topUV = new float[]{0, min, height, max};

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(16, height, min), new Vector3f(16 + height, height, max),
                            renderColor, topUV,
                            sheet.top[15], EnumFacing.UP, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(16 + height, 0, min), new Vector3f(16 + height, height, max),
                            renderColor, edgeUV,
                            sheet.top[15], EnumFacing.EAST, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(16, 0, min), new Vector3f(16 + height, height, min),
                            renderColor, edgeUVFlipped,
                            sheet.edge, EnumFacing.NORTH, rot, false
                    )
            );

            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(16, 0, max), new Vector3f(16 + height, height, max),
                            renderColor, edgeUV,
                            sheet.edge, EnumFacing.SOUTH, rot, false
                    )
            );
        }
    }

    public void addWire(PartWireBase wire, WireSheet sheet, List<BakedQuad> quads) {
        WireFace side = wire.location;
        int renderColorWire = wire.getRenderColor();
        int renderColor = renderColorWire == -1 ? renderColorWire :
                (0xFF000000 | (renderColorWire & 0x00FF00) | ((renderColorWire & 0xFF0000) >> 16) | ((renderColorWire & 0x0000FF) << 16));

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
                wire == null ? true : wire.connectsAny(dirs[0]),
                wire == null ? true : wire.connectsAny(dirs[1]),
                wire == null ? true : wire.connectsAny(dirs[2]),
                wire == null ? true : wire.connectsAny(dirs[3])
        };
        int cmc = (connectionMatrix[0] ? 8 : 0) | (connectionMatrix[1] ? 4 : 0) | (connectionMatrix[2] ? 2 : 0) | (connectionMatrix[3] ? 1 : 0);

        boolean[] cornerConnectionMatrix = new boolean[]{
                wire == null ? true : wire.connectsCorner(dirs[0]),
                wire == null ? true : wire.connectsCorner(dirs[1]),
                wire == null ? true : wire.connectsCorner(dirs[2]),
                wire == null ? true : wire.connectsCorner(dirs[3])
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
                RenderUtils.BAKERY.makeBakedQuad(
                        from, to,
                        renderColor, new float[]{from.getX(), from.getZ(), to.getX(), to.getZ()},
                        sheet.top[cmc], EnumFacing.UP,
                        rot, true
                )
        );

        from.setY(0.0F);
        to.setY(0.0F);
        quads.add(
                RenderUtils.BAKERY.makeBakedQuad(
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
        quads.add(
                RenderUtils.BAKERY.makeBakedQuad(
                        fromX, toX,
                        renderColor, new float[]{fromX.getZ(), fromX.getY(), toX.getZ(), toX.getY()},
                        sheet.side, EnumFacing.WEST, rot, false
                )
        );

        // getIcon(false, cmc == 0 || cmc == 4, crossroadsZ, EnumFacing.NORTH)
        quads.add(
                RenderUtils.BAKERY.makeBakedQuad(
                        fromZ, toZ,
                        renderColor, new float[]{toZ.getX(), fromZ.getY(), fromZ.getX(), toZ.getY()},
                        sheet.side, EnumFacing.NORTH, rot, false
                )
        );

        fromX.setX(max);
        toX.setX(max);

        fromZ.setZ(max);
        toZ.setZ(max);

        // getIcon(false, cmc == 2, crossroadsX, EnumFacing.EAST)
        quads.add(
                RenderUtils.BAKERY.makeBakedQuad(
                        fromX, toX,
                        renderColor, new float[]{toX.getZ(), fromX.getY(), fromX.getZ(), toX.getY()},
                        sheet.side, EnumFacing.EAST, rot, false
                )
        );

        // getIcon(false, cmc == 0 || cmc == 8, crossroadsZ, EnumFacing.SOUTH)
        quads.add(
                RenderUtils.BAKERY.makeBakedQuad(
                        fromZ, toZ,
                        renderColor, new float[]{fromZ.getX(), fromZ.getY(), toZ.getX(), toZ.getY()},
                        sheet.side, EnumFacing.SOUTH, rot, false
                )
        );

        // Edge faces
        float[] edgeUV = new float[]{min, minH, max, maxH};
        float[] edgeUVFlipped = new float[]{max, minH, min, maxH};

        if (connectionMatrix[0]) {
            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, minH, 0.0F), new Vector3f(max, maxH, 0.0F),
                            renderColor, edgeUVFlipped,
                            sheet.edge, EnumFacing.NORTH, rot, false
                    )
            );
        }

        if (connectionMatrix[1]) {
            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(min, minH, 16.0F), new Vector3f(max, maxH, 16.0F),
                            renderColor, edgeUV,
                            sheet.edge, EnumFacing.SOUTH, rot, false
                    )
            );
        }

        if (connectionMatrix[2]) {
            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(0.0F, minH, min), new Vector3f(0.0F, maxH, max),
                            renderColor, edgeUV,
                            sheet.edge, EnumFacing.WEST, rot, false
                    )
            );
        }

        if (connectionMatrix[3]) {
            quads.add(
                    RenderUtils.BAKERY.makeBakedQuad(
                            new Vector3f(16.0F, minH, min), new Vector3f(16.0F, maxH, max),
                            renderColor, edgeUVFlipped,
                            sheet.edge, EnumFacing.EAST, rot, false
                    )
            );
        }

        EnumFacing[] dirs0 = WireUtils.getConnectionsForRender(WireFace.DOWN);
        for (int i = 0; i < 4; i++) {
            if (cornerConnectionMatrix[i]) {
                addCorner(wire, sheet, dirs0[i], renderColor, quads);
            }
        }
    }

    @Override
    public IBakedModel bake(PartWireBase wire, boolean isItem, BlockRenderLayer layer) {
        WireSheet sheet = sheetMap.get(wire.getWireType());
        if (sheet == null) {
            return null;
        }

        SimpleBakedModel model = new SimpleBakedModel(this);
        model.setParticle(sheet.particle);
        addWire(wire, sheet, model.getQuads(null, null, 0));

        return model;
    }

    @Override
    public PartWireBase fromItemStack(ItemStack stack) {
        int md = stack.getItemDamage();
        if (stackMap.containsKey(md)) {
            return stackMap.get(md);
        } else {
            PartWireBase wire = PartWireProvider.createPart(md >> 1);
            wire.location = ItemWire.isFreestanding(stack) ? WireFace.CENTER : WireFace.DOWN;
            wire.setConnectionsForItemRender();

            stackMap.put(md, wire);
            return wire;
        }
    }
}
