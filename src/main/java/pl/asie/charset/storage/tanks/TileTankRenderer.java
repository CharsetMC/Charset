package pl.asie.charset.storage.tanks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.utils.ProxiedBlockAccess;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.pipes.pipe.SpecialRendererPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by asie on 2/11/17.
 */
public class TileTankRenderer extends FastTESR<TileTank> {
    private final BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

    private final class FluidColorTransformer implements ModelTransformer.IVertexTransformer {
        private final int color;

        public FluidColorTransformer(int color) {
            this.color = color;
        }

        @Override
        public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
            if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
                for (int i = 0; i < Math.min(data.length, 4); i++) {
                    data[i] = data[i] * ((color >> (i * 8)) & 0xFF) / 255.0f;
                }
            }
            return data;
        }
    }

    private final class TankBlockAccess extends ProxiedBlockAccess {
        private final int fluidLight;

        public TankBlockAccess(IBlockAccess access, int fluidLight) {
            super(access);
            this.fluidLight = fluidLight;
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return access.getCombinedLight(pos, Math.max(fluidLight, lightValue));
        }
    }

    @Override
    public void renderTileEntityFast(@Nonnull TileTank te, double x, double y, double z, float partialTicks, int destroyStage, @Nonnull VertexBuffer vertexBuffer) {
        boolean isCarrying = te.getWorld() != null;
        if (te.fluidStack == null || (!isCarrying && te.getBottomTank() != te))
            return;

        BlockPos pos = te.getPos();
        vertexBuffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = map.getTextureExtry(te.fluidStack.getFluid().getStill().toString());
        if (sprite == null) {
            sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
            if (sprite == null) return;
        }

        FluidStack contents = !isCarrying ? te.getContents() : te.fluidStack;
        int tankCount = !isCarrying ? (te.getCapacity() / TileTank.CAPACITY) : TileTank.CAPACITY;
        float height = (float) (contents.amount) / TileTank.CAPACITY;
        SimpleBakedModel smodel = new SimpleBakedModel();

        Vector3f from = new Vector3f(1.025f, 0.025f, 1.025f);
        Vector3f to = new Vector3f(14.975f, Math.min(16 * tankCount - 0.025f, height * 16), 14.975f);

        for (EnumFacing facing : EnumFacing.VALUES) {
            smodel.addQuad(null, RenderUtils.createQuad(from, to, facing, sprite, -1));
        }

        IBakedModel model;
        int color = te.fluidStack.getFluid().getColor(te.fluidStack);
        int light = te.fluidStack.getFluid().getLuminosity();

        if (color == -1) {
            model = smodel;
        } else {
            model = ModelTransformer.transform(smodel, te.fluidStack.getFluid().getBlock().getDefaultState(), 0L, new FluidColorTransformer(color));
        }

        renderer.renderModel(new TankBlockAccess(getWorld(), light), model, te.fluidStack.getFluid().getBlock().getDefaultState(), pos, vertexBuffer, false, 0L);
        vertexBuffer.setTranslation(0, 0, 0);
    }
}
