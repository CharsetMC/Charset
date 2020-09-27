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
import net.minecraft.tileentity.TileEntity;
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
                    data[i] = data[i] * ((color >> ((i < 3 ? (2 - i) : i) * 8)) & 0xFF) / 255.0f;
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
    public static void renderModel(IBlockAccess access, BlockPos pos, BufferBuilder buffer, TileTank tank, FluidStack contents, int tankCount) {
        IBlockState state = getFluidState(contents, Blocks.AIR.getDefaultState());
        IBakedModel model;

        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = map.getTextureExtry(contents.getFluid().getStill().toString());
        if (sprite == null) {
            sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
            if (sprite == null) return;
        }

        float height = (float) (contents.amount) / TileTank.CAPACITY;
        SimpleBakedModel smodel = new SimpleBakedModel();

        int color = contents.getFluid().getColor(contents);

        Vector3f from = new Vector3f(1.025f, 0.025f, 1.025f);
        Vector3f to = new Vector3f(14.975f, Math.min(16 * tankCount - 0.025f, height * 16), 14.975f);

        smodel.addQuad(null, RenderUtils.createQuad(from, to, EnumFacing.DOWN, sprite, -1));
        smodel.addQuad(null, RenderUtils.createQuad(from, to, EnumFacing.UP, sprite, -1));

        for (int i = 0; i < to.y; i += 16) {
            Vector3f fromL = new Vector3f(from.x, (from.y > i ? from.y : i), from.z);
            Vector3f toL = new Vector3f(to.x, (to.y < (i + 16) ? to.y : i + 16), to.z);

            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                smodel.addQuad(null, RenderUtils.createQuad(fromL, toL, facing, sprite, -1));
            }
        }

        if (color == -1) {
            model = smodel;
        } else {
            try {
                model = ModelTransformer.transform(smodel, state, 0L, new FluidColorTransformer(color));
            } catch (ModelTransformer.TransformationFailedException e) {
                // ignore, could be worse
                model = smodel;
            }
        }

        int light = contents.getFluid().getLuminosity();
        IBlockAccess tankAccess = new TankBlockAccess(access, light);

        renderer.renderModel(tankAccess, model, state, pos, buffer, false, 0L);
    }

    @Override
    public void renderTileEntityFast(@Nonnull TileTank te, double x, double y, double z, float partialTicks, int destroyStage, float todo_figure_me_out, @Nonnull BufferBuilder vertexBuffer) {
        BlockPos pos = te.getPos();

        if (te.getWorld() != null) {
            TileEntity lowerTile = te.getWorld().getTileEntity(pos.down());
            if (lowerTile instanceof TileTank && ((TileTank) lowerTile).connects(te) && te.connects((TileTank) lowerTile)) {
                return;
            }
        }

        if (te.getWorld() != null) {
            // TODO: Hack - tank caching does not properly update on client
            te.findBottomTank();
        }
        FluidStack contents = te.getContents();
        int tankCount = te.getCapacity() / TileTank.CAPACITY;

        if (contents == null || contents.getFluid() == null || contents.amount <= 0)
            return;

        vertexBuffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

        renderModel(getWorld(), pos, vertexBuffer, te, contents, tankCount);

        vertexBuffer.setTranslation(0, 0, 0);
    }
}
