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
import pl.asie.charset.lib.utils.RenderUtils;

public class CrateModel extends ModelFactory<CrateCacheInfo> {
    public final ModelColorizer<CrateCacheInfo> colorizer = new ModelColorizer<CrateCacheInfo>(this) {
        @Override
        public int colorMultiplier(CrateCacheInfo info, int tintIndex) {
            if (tintIndex == 0) {
                return ColorLookupHandler.INSTANCE.getColor(info.plank, RenderUtils.AveragingMode.FULL);
            } else if (tintIndex == 1) {
                return RenderUtils.multiplyColor(
                        ColorLookupHandler.INSTANCE.getColor(info.plank, RenderUtils.AveragingMode.FULL),
                        0x989898
                );
            }
            return -1;
        }
    };

    private static TextureAtlasSprite baseSprite, borderSprite, crossSprite;

    public CrateModel() {
        super(BlockCrate.PROPERTY, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    @Override
    public IBakedModel bake(CrateCacheInfo crate, boolean isItem, BlockRenderLayer layer) {
        SimpleBakedModel model = new SimpleBakedModel(this);
        Vector3f[] from = new Vector3f[3];
        Vector3f[] to = new Vector3f[3];
        float zOffset = 0.005f * 16;
        for (int i = 0; i < 3; i++) {
            float zi = i * zOffset;
            if (i > 0) {
                zi += 1 - zOffset * 3;
            }
            from[i] = new Vector3f(zi, zi, zi);
            to[i] = new Vector3f(16 - zi, 16 - zi, 16 - zi);
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            model.addQuad(facing, RenderUtils.bakeFace(from[2], to[2], facing, baseSprite, 0));
            model.addQuad(facing, RenderUtils.bakeFace(from[1], to[1], facing, crossSprite, 1));
            model.addQuad(facing, RenderUtils.bakeFace(from[0], to[0], facing, borderSprite, 1));

            // inner border
            Vector3f fromIB = new Vector3f(from[0]);
            Vector3f toIB = new Vector3f(to[0]);
            float v = 15 + zOffset;
            switch (facing.getAxis()) {
                case X:
                    fromIB.translate(v, 0, 0);
                    toIB.translate(-v, 0, 0);
                    break;
                case Y:
                    fromIB.translate(0, v, 0);
                    toIB.translate(0, -v, 0);
                    break;
                case Z:
                    fromIB.translate(0, 0, v);
                    toIB.translate(0, 0, -v);
                    break;
            }
            model.addQuad(null, RenderUtils.bakeFace(fromIB, toIB, facing.getOpposite(), borderSprite, 1));
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
        borderSprite = map.registerSprite(new ResourceLocation("charsetstorage:blocks/crate/border"));
        crossSprite = map.registerSprite(new ResourceLocation("charsetstorage:blocks/crate/cross"));
    }
}
