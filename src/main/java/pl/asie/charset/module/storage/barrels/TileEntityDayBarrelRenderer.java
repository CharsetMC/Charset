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

package pl.asie.charset.module.storage.barrels;

import com.elytradev.mirage.lighting.Light;
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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.utils.*;
import pl.asie.charset.module.tweak.dynlights.CharsetTweakDynamicLights;

import java.util.Calendar;

public class TileEntityDayBarrelRenderer extends TileEntitySpecialRenderer<TileEntityDayBarrel> {
    static final TCharIntMap fontMap = new TCharIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, (char) 0, -1);

    static {
        String fontIdx = "0123" +
                "4567" +
                "89*+" +
                "i! c";

        for (int i = 0; i < 16; i++) {
            if (fontIdx.charAt(i) != ' ') {
                fontMap.put(fontIdx.charAt(i), i);
            }
        }
    }

    void doDraw(TileEntityDayBarrel barrel, ItemStack is, float partialTicks) {
        Orientation bo = barrel.orientation;
        EnumFacing face = bo.facing;
        if (SpaceUtils.sign(face) == 1) {
            GlStateManager.translate(face.getDirectionVec().getX(), face.getDirectionVec().getY(), face.getDirectionVec().getZ());
        }
        GlStateManager.translate(
                0.5*(1 - Math.abs(face.getDirectionVec().getX())),
                0.5*(1 - Math.abs(face.getDirectionVec().getY())),
                0.5*(1 - Math.abs(face.getDirectionVec().getZ()))
                );

        Quaternion quat = Quaternion.fromOrientation(bo.getSwapped());
        quat.glRotate();
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0.25, 0.25 - 1.0/16.0, -1.0/128.0);
        if (barrel.upgrades.contains(BarrelUpgrade.HOPPING)) {
            double time = barrel.getWorld().getTotalWorldTime() + partialTicks;
            if (Math.sin(time/22.5) > 0) {
                double delta = Math.max(0, Math.sin(time/2.25)/16);
                GlStateManager.translate(0, delta, 0);
            }
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        boolean hasLabel = renderItemCount(is, barrel);
        handleRenderItem(is, barrel, hasLabel);
    }

    @Override
    public void render(TileEntityDayBarrel barrel, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (barrel == null) {
            return;
        }

        ItemStack is = barrel.getItemUnsafe();
        if (barrel.getItemCount() <= 0) {
            return;
        }

        // Detect "non-world" TESR rendering
        TileEntity standingTile = barrel.getWorld().getTileEntity(barrel.getPos());

        if (standingTile == barrel) {
            EnumFacing facing = barrel.orientation.facing;
            BlockPos facingPos = barrel.getPos().offset(facing);
            if (barrel.getWorld().isSideSolid(facingPos, facing.getOpposite())) {
                return;
            }
        }

        Minecraft.getMinecraft().mcProfiler.startSection("barrels");
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        doDraw(barrel, is, partialTicks);

        GlStateManager.popMatrix();
        Minecraft.getMinecraft().mcProfiler.endSection();

        GlStateManager.disableBlend();
        RenderHelper.enableStandardItemLighting();
    }

    String getCountLabel(ItemStack item, TileEntityDayBarrel barrel) {
        if (barrel.upgrades.contains(BarrelUpgrade.INFINITE)) {
            return "i";
        }
        int ms = barrel.getStackDivisor();
        int count = barrel.getItemCount();
        if (count == 1) {
            return "";
        }
        String t = "";
        if (ms == 1 || count == ms) {
            t += count;
        } else {
            int q = count / ms;
            if (q > 0) {
                t += (count / ms) + "*" + ms;
            }
            int r = (count % ms);
            if (r != 0) {
                if (q > 0) {
                    t += "+";
                }
                t += r;
            }
        }

        Calendar cal = CharsetLib.calendar.get();
        if (cal.get(Calendar.MONTH) == 8 && cal.get(Calendar.DAY_OF_MONTH) == 9) {
            IBlockState state = ItemUtils.getBlockState(item);
            if (state != null && (state.getMaterial() == Material.ICE || state.getMaterial() == Material.PACKED_ICE)) {
                if (t.startsWith("9*")) {
                    t = "c" + t.substring(1);
                }

                if (t.endsWith("+9")) {
                    t = t.substring(0, t.length() - 1) + "c";
                }

                if (t.equals("9")) {
                    t = "c";
                }
            }
        }

        if (barrel.canLose()) {
            t = "!" + t + "!";
        }

        return t;
    }
    
    boolean renderItemCount(ItemStack item, TileEntityDayBarrel barrel) {
        if (!CharsetStorageBarrels.renderBarrelText) return false;

        final String t = getCountLabel(item, barrel);
        if (t.isEmpty()) {
            return false;
        }

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.disableLighting();

        final TextureAtlasSprite font = BarrelModel.INSTANCE.font;
        final int len = t.length();
        double char_width = 1.0/10.0;
        double char_height = 1.0/10.0;
        double char_y_offset = 0;

        if (len > 8) {
            char_width *= 8.0 / (double) len;
            char_height *= 8.0 / (double) len;
            char_y_offset = -((1.0/10.0) - char_height);
        }

        final Tessellator tessI = Tessellator.getInstance();
        BufferBuilder tess = tessI.getBuffer();
        tess.setTranslation(-char_width * len / 2 + 0.25, -char_height - 1F/32F + char_y_offset, 0);
        tess.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); // 3 double vertex positions + 2 double UV positions
        double du = (font.getMaxU() - font.getMinU()) / 4;
        double dv = (font.getMaxV() - font.getMinV()) / 4;
        double u = font.getMinU();
        double v = font.getMinV();
        for (int i = 0; i < len; i++) {
            int ci = fontMap.get(t.charAt(i));
            if (ci >= 0) {
                int x = ci&3, y = ci>>2;
                double IX = i * char_width;
                final double dy = 1.0 - (1.0 / 256.0);
                tess.pos(IX + char_width, 0, 0).tex(u + (x + 1) * du, v + y * dv).endVertex();
                tess.pos(IX, 0, 0).tex(u + x * du, v + y * dv).endVertex();
                tess.pos(IX, char_height, 0).tex(u + x * du, v + (y + dy) * dv).endVertex();
                tess.pos(IX + char_width, char_height, 0).tex(u + (x + 1) * du, v + (y + dy) * dv).endVertex();
            }
        }
        tessI.draw();
        tess.setTranslation(0, 0, 0);

        GlStateManager.enableLighting();
        GlStateManager.rotate(180, 0, 0, 1);
        return true;
    }

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public void handleRenderItem(ItemStack is, TileEntityDayBarrel barrel, boolean hasLabel) {
        if (!CharsetStorageBarrels.renderBarrelItem) return;
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0, 1, 0);
        float labelD = hasLabel ? 0F : -1F/16F;
        GlStateManager.translate(0.25, -0.25 - labelD, 0);

        if (CharsetStorageBarrels.renderBarrelItem3D) {
            boolean isBlock = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(is, barrel.getWorld(), null).isGui3d();
            if (isBlock) {
                Block block = Block.getBlockFromItem(is.getItem());
                GlStateManager.scale(0.75F, 0.75F, 0.75F);
                if (block.getDefaultState().isFullCube()) {
                    GlStateManager.translate(0, 0, -0.1);
                }
            } else {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(is, ItemCameraTransforms.TransformType.FIXED);
        } else {
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(is, barrel.getWorld(), null);

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

            GlStateManager.scale(0.5F, 0.5F, 0.025F);
            model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GUI, false);

            Minecraft.getMinecraft().getRenderItem().renderItem(is, model);
            GlStateManager.disableRescaleNormal();
            Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
        GlStateManager.popMatrix();
    }
}
