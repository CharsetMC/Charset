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

package pl.asie.charset.module.misc.shelf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import pl.asie.charset.lib.Properties;

/**
 * Created by asie on 2/13/17.
 */
public class TileShelfRenderer extends TileEntitySpecialRenderer<TileShelf> {
    private final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    @Override
    public void render(TileShelf tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState state = getWorld().getBlockState(tile.getPos());
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        for (int i = 0; i < 4; i++) {
            ItemStack stack = tile.handler.getStackInSlot(14 + i);
            if (!stack.isEmpty()) {
                Vec3d offset = new Vec3d(
                        ((i & 1) != 0) ? 11.5F / 16F : 4.5F / 16F,
                        ((i & 2) != 0) ? 13 / 16F : 5 / 16F,
                        (state.getValue(BlockShelf.BACK)) ? 4F / 16F : 12F / 16F);

                GlStateManager.pushMatrix();
                IBakedModel model = renderItem.getItemModelWithOverrides(stack, tile.getWorld(), null);
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate(state.getValue(Properties.FACING4).getHorizontalAngle(), 0, 1, 0);
                GlStateManager.translate(offset.x, offset.y, offset.z);
                GlStateManager.scale(0.25F, 0.25F, 0.25F);

                //
                //GlStateManager.translate(offset.x, offset.y,  offset.z);
                //GlStateManager.scale(0.25F, 0.25F, 0.25F);]

                renderItem.renderItem(stack, model);
                GlStateManager.popMatrix();
            }
        }
    }
}
