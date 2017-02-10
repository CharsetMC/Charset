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

package pl.asie.charset.storage.barrels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.SpaceUtils;

public class BarrelEventListener {
    private Vec3d getCameraPos(double partialTicks) {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        return new Vec3d(
            camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks,
            camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks,
            camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks
        );
    }

    private void drawArrowHighlight(EntityPlayer player, RayTraceResult trace, Vec3d cameraPos) {
        Orientation orientation = SpaceUtils.getOrientation(player, trace.sideHit, trace.hitVec.subtract(new Vec3d(trace.getBlockPos())));
        if (orientation.top.getDirectionVec().getY() == 1) {
            /*
             * The purpose of this is two-fold:
             *     - It renders at the wrong spot when pointing upwards on a vertical face
             *     - You totally don't really need it in this case
             */
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-cameraPos.xCoord, -cameraPos.yCoord, -cameraPos.zCoord);
        EnumFacing fd = trace.sideHit;
        BlockPos v = trace.getBlockPos().add(fd.getDirectionVec());
        GlStateManager.translate(v.getX(), v.getY(), v.getZ());
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(2.0F);

        {
            EnumFacing face = orientation.facing;
            if (SpaceUtils.sign(face) == 1) {
                GlStateManager.translate(
                        face.getDirectionVec().getX(),
                        face.getDirectionVec().getY(),
                        face.getDirectionVec().getZ()
                );
            }
            float d = -2F;
            GlStateManager.translate(
                    d * fd.getDirectionVec().getX(),
                    d * fd.getDirectionVec().getY(),
                    d * fd.getDirectionVec().getZ()
            );
            GlStateManager.translate(
                    0.5 * (1 - Math.abs(face.getDirectionVec().getX())),
                    0.5 * (1 - Math.abs(face.getDirectionVec().getY())),
                    0.5 * (1 - Math.abs(face.getDirectionVec().getZ()))
            );

            GlStateManager.glBegin(GL11.GL_LINE_LOOP);
            float mid_x = orientation.facing.getDirectionVec().getX();
            float mid_y = orientation.facing.getDirectionVec().getY();
            float mid_z = orientation.facing.getDirectionVec().getZ();

            float top_x = mid_x + orientation.top.getDirectionVec().getX() / 2F;
            float top_y = mid_y + orientation.top.getDirectionVec().getY() / 2F;
            float top_z = mid_z + orientation.top.getDirectionVec().getZ() / 2F;

            float bot_x = mid_x - orientation.top.getDirectionVec().getX() / 2F;
            float bot_y = mid_y - orientation.top.getDirectionVec().getY() / 2F;
            float bot_z = mid_z - orientation.top.getDirectionVec().getZ() / 2F;

            EnumFacing r = SpaceUtils.rotateCounterclockwise(orientation.facing, orientation.top);
            float right_x = r.getDirectionVec().getX() / 2F;
            float right_y = r.getDirectionVec().getY() / 2F;
            float right_z = r.getDirectionVec().getZ() / 2F;

            //GL11.glVertex3f(mid_x, mid_y, mid_z);
            GlStateManager.glVertex3f(top_x, top_y, top_z);
            GlStateManager.glVertex3f(mid_x + right_x, mid_y + right_y, mid_z + right_z);
            d = 0.25F;
            GlStateManager.glVertex3f(mid_x + right_x * d, mid_y + right_y * d, mid_z + right_z * d);
            GlStateManager.glVertex3f(bot_x + right_x * d, bot_y + right_y * d, bot_z + right_z * d);
            d = -0.25F;
            GlStateManager.glVertex3f(bot_x + right_x * d, bot_y + right_y * d, bot_z + right_z * d);
            GlStateManager.glVertex3f(mid_x + right_x * d, mid_y + right_y * d, mid_z + right_z * d);
            GlStateManager.glVertex3f(mid_x - right_x, mid_y - right_y, mid_z - right_z);
            GlStateManager.glEnd();
        }

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderArrowHighlight(RenderWorldLastEvent event) {
        if (Minecraft.isGuiEnabled()) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            if (player != null) {
                ItemStack is = player.getHeldItem(EnumHand.MAIN_HAND);
                if (!is.isEmpty() && is.getItem() == CharsetStorageBarrels.barrelItem) {
                    RayTraceResult mop = mc.objectMouseOver;
                    if (mop != null && mop.hitVec != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                        drawArrowHighlight(player, mop, getCameraPos(event.getPartialTicks()));
                    }
                }
            }
        }
    }
}
