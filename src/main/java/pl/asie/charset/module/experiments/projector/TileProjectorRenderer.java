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

package pl.asie.charset.module.experiments.projector;

import gnu.trove.impl.Constants;
import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.module.storage.barrels.BarrelModel;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import java.util.Calendar;

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
        RenderHelper.enableStandardItemLighting();
    }

    public void handleRenderItem(ItemStack is, TileProjector projector) {
        GlStateManager.translate(-0.25f, -0.25f, 0);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.pushAttrib();
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(is, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();

        /* GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0, 1, 0);
        float labelD = -1F / 16F;
        GlStateManager.translate(0.25, -0.25 - labelD, 0);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(is, projector.getWorld(), null);
        model = model.handlePerspective(ItemCameraTransforms.TransformType.GROUND).getKey();

        // TODO: This might be the ugliest hack in all of barrels. FIXME
        if (model.isGui3d()) {
            RenderHelper.enableStandardItemLighting();
            if (!model.isBuiltInRenderer()) {
                model = ModelTransformer.transform(model, null, 0, (quad, element, data) -> {
                    if (element.getUsage() == VertexFormatElement.EnumUsage.NORMAL) {
                        data[0] /= 1.5f;
                        data[2] *= 1.7f;
                    }
                    return data;
                });
            }
        } else {
            RenderHelper.enableStandardItemLighting();
        }

        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GUI, false);

        Minecraft.getMinecraft().getRenderItem().renderItem(is, model);
        GlStateManager.disableRescaleNormal();
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

        GlStateManager.popMatrix(); */
    }
}
