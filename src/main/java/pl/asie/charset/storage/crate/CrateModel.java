package pl.asie.charset.storage.crate;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.ModelColorizer;
import pl.asie.charset.lib.render.ModelFactory;
import pl.asie.charset.lib.render.SimpleBakedModel;
import pl.asie.charset.lib.render.SpritesheetFactory;
import pl.asie.charset.lib.utils.RenderUtils;

public class CrateModel extends ModelFactory<CrateCacheInfo> {
    public final ModelColorizer<CrateCacheInfo> colorizer = new ModelColorizer<CrateCacheInfo>(this) {
        @Override
        public int colorMultiplier(CrateCacheInfo info, int tintIndex) {
            if (info.plank != null) {
                if (tintIndex == 0) {
                    return ColorLookupHandler.INSTANCE.getColor(info.plank, RenderUtils.AveragingMode.FULL);
                } else if (tintIndex == 1) {
                    return RenderUtils.multiplyColor(
                            ColorLookupHandler.INSTANCE.getColor(info.plank, RenderUtils.AveragingMode.FULL),
                            0x989898
                    );
                }
            }
            return -1;
        }
    };

    private static final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
            {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH}
    };

    private static final Vector3f[] from = new Vector3f[3];
    private static final Vector3f[] to = new Vector3f[3];
    private static final float zOffset = 0.005f * 16;
    private static TextureAtlasSprite baseSprite, crossSprite;
    private static TextureAtlasSprite[] borderSprite;

    static {
        for (int i = 0; i < 3; i++) {
            float zi = i * zOffset;
            if (i > 0) {
                zi += 1 - zOffset * 3;
            }
            from[i] = new Vector3f(zi, zi, zi);
            to[i] = new Vector3f(16 - zi, 16 - zi, 16 - zi);
        }
    }

    public CrateModel() {
        super(BlockCrate.PROPERTY, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    private void set(Vector3f vector3f, EnumFacing.Axis axis, float v) {
        switch (axis) {
            case X:
                vector3f.set(v, vector3f.y, vector3f.z);
                break;
            case Y:
                vector3f.set(vector3f.x, v, vector3f.z);
                break;
            case Z:
                vector3f.set(vector3f.x, vector3f.y, v);
                break;
        }
    }

    @Override
    public IBakedModel bake(CrateCacheInfo crate, boolean isItem, BlockRenderLayer layer) {
        SimpleBakedModel model = new SimpleBakedModel(this);
        for (EnumFacing facing : EnumFacing.VALUES) {
            EnumFacing[][] CONNECTION_DIRS2 = new EnumFacing[][]{
                    {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
                    {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
                    {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
                    {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
                    {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH},
                    {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH}
            };

            Vector3f fromBase = new Vector3f(from[2]);
            Vector3f toBase = new Vector3f(to[2]);
            Vector3f fromCross = new Vector3f(from[1]);
            Vector3f toCross = new Vector3f(to[1]);

            int bsPos = 0;
            for (int i = 0; i < 4; i++) {
                EnumFacing dir = CONNECTION_DIRS2[facing.ordinal()][i];
                if (!crate.isConnected(dir)) {
                    bsPos |= (1 << (3 - i));
                } else {
                    // Check for corner line
                    EnumFacing.Axis diffAxis = null;
                    for (EnumFacing.Axis axis : EnumFacing.Axis.values()) {
                        if (axis != dir.getAxis() && axis != facing.getAxis()) {
                            diffAxis = axis;
                        }
                    }
                    int corner1 = 0;
                    if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                        corner1 |= (1 << (2 - facing.getAxis().ordinal()));
                    }
                    if (dir.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                        corner1 |= (1 << (2 - dir.getAxis().ordinal()));
                    }
                    int corner2 = corner1 | (1 << (2 - diffAxis.ordinal()));
                    if ((crate.isCorner(corner1) && crate.isCorner(corner2))) {
                        //bsPos |= (1 << (3 - i));
                    }

                    EnumFacing.AxisDirection axisDir = dir.getAxisDirection();
                    if (axisDir == EnumFacing.AxisDirection.POSITIVE) {
                        set(toBase, dir.getAxis(), 16);
                        set(toCross, dir.getAxis(), 16);
                    } else {
                        set(fromBase, dir.getAxis(), 0);
                        set(fromCross, dir.getAxis(), 0);
                    }
                }
            }

            model.addQuad(null, RenderUtils.bakeFace(fromBase, toBase, facing, baseSprite, 0));
            model.addQuad(null, RenderUtils.bakeFace(fromCross, toCross, facing, crossSprite, 1));
            if (bsPos != 0 && !crate.isConnected(facing)) {
                model.addQuad(facing, RenderUtils.bakeFace(from[0], to[0], facing, borderSprite[bsPos], 1));

                // inner border
                Vector3f fromIB = new Vector3f(from[0]);
                Vector3f toIB = new Vector3f(to[0]);
                float v = 15 + zOffset;
                switch (facing.getAxis()) {
                    case X:
                        fromIB.translate(v, 0, 0);
                        toIB.translate(-v, 0, 0);
                        bsPos = (bsPos & 12) | ((bsPos & 2) >> 1) | ((bsPos & 1) << 1);
                        break;
                    case Y:
                        fromIB.translate(0, v, 0);
                        toIB.translate(0, -v, 0);
                        bsPos = (bsPos & 3) | ((bsPos & 8) >> 1) | ((bsPos & 4) << 1);
                        break;
                    case Z:
                        fromIB.translate(0, 0, v);
                        toIB.translate(0, 0, -v);
                        bsPos = (bsPos & 12) | ((bsPos & 2) >> 1) | ((bsPos & 1) << 1);
                        break;
                }

                model.addQuad(null, RenderUtils.bakeFace(fromIB, toIB, facing.getOpposite(), borderSprite[bsPos], 1));
            }
        }

        for (int z = 0; z < 2; z++) {
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 2; x++) {
                    if (crate.isCorner(x, y, z)) {
                        Vector3f fFrom = new Vector3f(
                                x == 1 ? 15 + zOffset : 0,
                                y == 1 ? 15 + zOffset : 0,
                                z == 1 ? 15 + zOffset : 0
                        );
                        Vector3f fTo = new Vector3f(fFrom).translate(1 - zOffset, 1 - zOffset, 1 - zOffset);
                        // remember: these are meant to be opposite!
                        for (EnumFacing facing : EnumFacing.VALUES) {
                           model.addQuad(null, RenderUtils.bakeFace(fFrom, fTo, facing, borderSprite[15], 1));
                        }
                        //model.addQuad(null, RenderUtils.bakeFace(fFrom, fTo, x == 1 ? EnumFacing.WEST : EnumFacing.EAST, borderSprite[15], 1));
                        //model.addQuad(null, RenderUtils.bakeFace(fFrom, fTo, y == 1 ? EnumFacing.DOWN : EnumFacing.UP, borderSprite[15], 1));
                        //model.addQuad(null, RenderUtils.bakeFace(fFrom, fTo, z == 1 ? EnumFacing.NORTH : EnumFacing.SOUTH, borderSprite[15], 1));
                    }
                }
            }
        }
        return model;
    }

    @Override
    public CrateCacheInfo fromItemStack(ItemStack stack) {
        TileEntityCrate crate = new TileEntityCrate();
        crate.loadFromStack(stack);
        return crate.getCacheInfo();
    }

    public static void onTextureLoad(TextureMap map) {
        baseSprite = map.registerSprite(new ResourceLocation("charsetstorage:blocks/crate/base"));
        borderSprite = SpritesheetFactory.register(map, new ResourceLocation("charsetstorage:blocks/crate/border"), 4, 4);
        crossSprite = map.registerSprite(new ResourceLocation("charsetstorage:blocks/crate/cross"));
    }
}
