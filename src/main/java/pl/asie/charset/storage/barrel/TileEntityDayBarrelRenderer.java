/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.storage.barrel;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.factorization.FzOrientation;
import pl.asie.charset.lib.factorization.Quaternion;
import pl.asie.charset.lib.factorization.SpaceUtil;
import pl.asie.charset.lib.render.ModelTransformer;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.storage.ModCharsetStorage;

import java.util.Calendar;

public class TileEntityDayBarrelRenderer extends TileEntitySpecialRenderer<TileEntityDayBarrel> {
    void doDraw(TileEntityDayBarrel barrel, ItemStack is, float partialTicks) {
        FzOrientation bo = barrel.orientation;
        EnumFacing face = bo.facing;
        if (SpaceUtil.sign(face) == 1) {
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
        if (barrel.type.isHopping()) {
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
    public void renderTileEntityAt(TileEntityDayBarrel barrel, double x, double y, double z, float partialTicks, int destroyStage) {
        if (barrel == null) {
            return;
        }

        ItemStack is = barrel.item;
        if (is == null || barrel.getItemCount() <= 0) {
            return;
        }

        EnumFacing facing = barrel.orientation.facing;
        BlockPos facingPos = barrel.getPos().offset(facing);
        if (barrel.getWorld().isSideSolid(facingPos, facing.getOpposite())) {
            return;
        }

        Minecraft.getMinecraft().mcProfiler.startSection("barrel");
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int l = barrel.getWorld().getCombinedLight(facingPos, barrel.getWorld().getSkylightSubtracted());
        int j = l % 65536;
        int k = l / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);

        doDraw(barrel, is, partialTicks);

        GlStateManager.popMatrix();
        Minecraft.getMinecraft().mcProfiler.endSection();

        GlStateManager.disableBlend();
        RenderHelper.enableStandardItemLighting();
    }

    String getCountLabel(ItemStack item, TileEntityDayBarrel barrel) {
        if (barrel.type == TileEntityDayBarrel.Type.CREATIVE) {
            return "i";
        }
        int ms = item.getMaxStackSize();
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
        if (barrel.canLose()) {
            t = "!" + t + "!";
        }

        Calendar cal = ModCharsetLib.calendar.get();
        if (cal.get(Calendar.MONTH) == 8 && cal.get(Calendar.DAY_OF_MONTH) == 9) {
            IBlockState state = ItemUtils.getBlockState(item);
            if (state != null && (state.getMaterial() == Material.ICE || state.getMaterial() == Material.PACKED_ICE)) {
                if (t.startsWith("9")) {
                    t = "c" + t.substring(1);
                }

                if (t.endsWith("9")) {
                    t = t.substring(0, t.length() - 1) + "c";
                }
            }
        }
        return t;
    }
    
    final String[] fontIdx = new String[] {
        "0123",
        "4567",
        "89*+",
        "i! c" // 'i' stands in for ∞, '!' stands in for '!!', 'c' stands in for U+2468
    };
    
    boolean renderItemCount(ItemStack item, TileEntityDayBarrel barrel) {
        if (!ModCharsetStorage.renderBarrelText) return false;
        final String t = getCountLabel(item, barrel);
        if (t.isEmpty()) {
            return false;
        }

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.disableLighting();

        final TextureAtlasSprite font = BarrelModel.font;
        final int len = t.length();
        final double char_width = 1.0/10.0;
        final double char_height = 1.0/10.0;
        final Tessellator tessI = Tessellator.getInstance(); //new Tessellator(len * 4);
        VertexBuffer tess = tessI.getBuffer();
        tess.setTranslation(-char_width * len / 2 + 0.25, -char_height - 1F/32F, 0);
        tess.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); // 3 double vertex positions + 2 double UV positions
        double du = (font.getMaxU() - font.getMinU()) / 4;
        double dv = (font.getMaxV() - font.getMinV()) / 4;
        double u = font.getMinU();
        double v = font.getMinV();
        for (int i = 0; i < len; i++) {
            char c = t.charAt(i);
            int x = 0, y = 0;
            boolean found = false;
            foundIdx: for (y = 0; y < fontIdx.length; y++) {
                String idx = fontIdx[y];
                for (x = 0; x < idx.length(); x++) {
                    if (c == idx.charAt(x)) {
                        found = true;
                        break foundIdx;
                    }
                }
            }
            if (!found) continue;
            double IX = i*char_width;
            final double dy = 1.0 - (1.0/256.0);
            tess.pos(IX + char_width, 0, 0).tex(u + (x + 1) * du, v + y * dv).endVertex();
            tess.pos(IX, 0, 0).tex(u + x * du, v + y * dv).endVertex();
            tess.pos(IX, char_height, 0).tex(u + x * du, v + (y + dy) * dv).endVertex();
            tess.pos(IX + char_width, char_height, 0).tex(u + (x + 1) * du, v + (y + dy) * dv).endVertex();
        }
        tessI.draw();
        tess.setTranslation(0, 0, 0);

        GlStateManager.enableLighting();
        GlStateManager.rotate(180, 0, 0, 1);
        return true;
    }

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public void handleRenderItem(ItemStack is, TileEntityDayBarrel barrel, boolean hasLabel) {
        if (!ModCharsetStorage.renderBarrelItem) return;
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0, 1, 0);
        float labelD = hasLabel ? 0F : -1F/16F;
        GlStateManager.translate(0.25, -0.25 - labelD, 0);

        if (ModCharsetStorage.renderBarrelItem3D) {
            boolean isBlock = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(is, null, null).isGui3d();
            boolean isFullBlock = false;
            Block block = Block.getBlockFromItem(is.getItem());
            if (block != null) {
                isFullBlock = block.getDefaultState().isFullCube();
            }

            if (isBlock) {
                GlStateManager.scale(0.75F, 0.75F, 0.75F);
                if (isFullBlock) {
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
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();

            GlStateManager.scale(0.5F, 0.5F, 0.02F);

            IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(is, null, null);
            model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GUI, false);

            // TODO: This might be the ugliest hack in all of barrels. FIXME
            if (model.isGui3d() && !model.isBuiltInRenderer()) {
                model = ModelTransformer.transform(model, null, 0, new ModelTransformer.IVertexTransformer() {
                    @Override
                    public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
                        if (element.getUsage() == VertexFormatElement.EnumUsage.NORMAL) {
                            data[0] /= 1.5f;
                            data[2] *= 1.7f;
                        }
                        return data;
                    }
                });
            }

            Minecraft.getMinecraft().getRenderItem().renderItem(is, model);
            GlStateManager.disableRescaleNormal();
            Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
        GlStateManager.popMatrix();
    }
}
