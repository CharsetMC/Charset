package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.utils.ProxiedBlockAccess;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nonnull;

public class TileTankRenderer extends FastTESR<TileTank> {
    private static final BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

    private static final class FluidColorTransformer implements ModelTransformer.IVertexTransformer {
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

    private static final class TankBlockAccess extends ProxiedBlockAccess {
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

    private static IBlockState getFluidState(FluidStack stack, IBlockState default_state) {
        return stack.getFluid().getBlock() != null ? stack.getFluid().getBlock().getDefaultState() : default_state;
    }

    // TODO: Rewrite this to render per-tank-block?
    public static void renderModel(IBlockAccess access, BlockPos pos, BufferBuilder buffer, FluidStack contents, int tankCount) {
        if (contents == null || contents.getFluid() == null || contents.amount <= 0)
            return;

        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = map.getTextureExtry(contents.getFluid().getStill().toString());
        if (sprite == null) {
            sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
            if (sprite == null) return;
        }

        float height = (float) (contents.amount) / TileTank.CAPACITY;
        SimpleBakedModel smodel = new SimpleBakedModel();

        int color = contents.getFluid().getColor(contents);
        int light = contents.getFluid().getLuminosity();

        Vector3f from = new Vector3f(1.025f, 0.025f, 1.025f);
        Vector3f to = new Vector3f(14.975f, Math.min(16 * tankCount - 0.025f, height * 16), 14.975f);
        IBlockAccess tankAccess = new TankBlockAccess(access, light);

        smodel.addQuad(null, RenderUtils.createQuad(from, to, EnumFacing.DOWN, sprite, -1));
        smodel.addQuad(null, RenderUtils.createQuad(from, to, EnumFacing.UP, sprite, -1));

        IBakedModel model;
        IBlockState state = getFluidState(contents, CharsetStorageTanks.tankBlock.getDefaultState());

        if (color == -1) {
            model = smodel;
        } else {
            model = ModelTransformer.transform(smodel, state, 0L, new FluidColorTransformer(color));
        }

        renderer.renderModel(tankAccess, model, contents.getFluid().getBlock() == null ? null : contents.getFluid().getBlock().getDefaultState(), pos, buffer, false, 0L);

        for (int i = 0; i < to.y; i += 16) {
            BlockPos pos1 = pos.offset(EnumFacing.UP, i / 16);
            Vector3f fromL = new Vector3f(from.x, (from.y > i ? from.y : i) - i, from.z);
            Vector3f toL = new Vector3f(to.x, (to.y < (i + 16) ? to.y : i + 16) - i, to.z);
            smodel = new SimpleBakedModel();

            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                smodel.addQuad(null, RenderUtils.createQuad(fromL, toL, facing, sprite, -1));
            }

            if (color == -1) {
                model = smodel;
            } else {
                model = ModelTransformer.transform(smodel, state, 0L, new FluidColorTransformer(color));
            }

            renderer.renderModel(tankAccess, model, state, pos1, buffer, false, 0L);
        }
    }

    @Override
    public void renderTileEntityFast(@Nonnull TileTank te, double x, double y, double z, float partialTicks, int destroyStage, float todo_figure_me_out, @Nonnull BufferBuilder vertexBuffer) {
        if (te.fluidStack == null || te.getBottomTank() != te)
            return;

        BlockPos pos = te.getPos();
        vertexBuffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

        FluidStack contents = te.getContents();
        int tankCount = te.getCapacity() / TileTank.CAPACITY;
        renderModel(getWorld(), pos, vertexBuffer, contents, tankCount);

        vertexBuffer.setTranslation(0, 0, 0);
    }
}
