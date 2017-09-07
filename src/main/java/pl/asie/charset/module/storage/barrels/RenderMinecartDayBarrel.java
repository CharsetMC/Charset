/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderMinecartDayBarrel extends RenderMinecart<EntityMinecartDayBarrel> {
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
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 0, 1);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightness(
                BarrelModel.INSTANCE, state, minecart.getBrightness(), true);
        GlStateManager.disableBlend();
        tesr.render(minecart.barrel, 0, 0, 0, partialTicks, 0, 1.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
