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

package pl.asie.charset.module.optics.projector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.lib.utils.SpaceUtils;

public class TileProjectorRenderer extends TileEntitySpecialRenderer<TileProjector> {
    @Override
    public void render(TileProjector projector, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (projector == null || projector.getStack().isEmpty()) {
            return;
        }

        ItemStack stack = projector.getStack();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        Orientation bo = projector.getOrientation();
        EnumFacing face = bo.top;
        if (SpaceUtils.sign(face) == 1) {
            GlStateManager.translate(face.getDirectionVec().getX(), face.getDirectionVec().getY(), face.getDirectionVec().getZ());
        }
        GlStateManager.translate(
                0.5*(1 - Math.abs(face.getDirectionVec().getX())),
                0.5*(1 - Math.abs(face.getDirectionVec().getY())),
                0.5*(1 - Math.abs(face.getDirectionVec().getZ()))
        );

        Quaternion quat = Quaternion.fromOrientation(bo);
        quat.glRotate();
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0.25, 0.25 - 1.0/16.0, 31.0/128.0);

        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        handleRenderItem(stack, projector);

        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
    }

    public void handleRenderItem(ItemStack is, TileProjector projector) {
        GlStateManager.translate(-0.25f, -0.1875f, 0);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        Minecraft.getMinecraft().getRenderItem().renderItem(is, ItemCameraTransforms.TransformType.FIXED);
    }
}
