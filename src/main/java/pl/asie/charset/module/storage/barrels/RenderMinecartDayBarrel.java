/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.storage.barrels;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;
import java.util.Random;

public class RenderMinecartDayBarrel extends RenderMinecart<EntityMinecartDayBarrel> {
    private static class ProxyBlockAccess implements IBlockAccess {
        private final IBlockAccess parent;
        private final int brightness;

        public ProxyBlockAccess(IBlockAccess access, int brightness) {
            this.parent = access;
            this.brightness = brightness;
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return parent.getTileEntity(pos);
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return brightness;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return parent.getBlockState(pos);
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return parent.isAirBlock(pos);
        }

        @Override
        public Biome getBiome(BlockPos pos) {
            return parent.getBiome(pos);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        @Override
        public WorldType getWorldType() {
            return parent.getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return parent.isSideSolid(pos, side, _default);
        }
    }

    private static final TileEntityDayBarrelRenderer tesr = new TileEntityDayBarrelRenderer();

    static {
        tesr.setRendererDispatcher(TileEntityRendererDispatcher.instance);
    }

    public RenderMinecartDayBarrel(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected void renderCartContents(EntityMinecartDayBarrel minecart, float partialTicks, IBlockState state) {
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.translate(-1, 0, 0);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        RenderHelper.disableStandardItemLighting();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-minecart.getPosition().getX(), -minecart.getPosition().getY(), -minecart.getPosition().getZ());

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer()
                .renderModelFlat(new ProxyBlockAccess(minecart.getEntityWorld(), minecart.getBrightnessForRender()), BarrelModel.INSTANCE, state, minecart.getPosition(), buffer, false, 0L);

        tessellator.draw();
        buffer.setTranslation(0, 0, 0);

        GlStateManager.disableBlend();
        tesr.render(minecart.barrel, 0, 0, 0, partialTicks, 0, 1.0f);
        GlStateManager.popMatrix();
    }
}
