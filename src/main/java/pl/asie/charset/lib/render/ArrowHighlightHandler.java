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

package pl.asie.charset.lib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.utils.EntityUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.SpaceUtils;

import java.util.IdentityHashMap;
import java.util.Map;

public class ArrowHighlightHandler {
    @FunctionalInterface
    public interface Checker {
        boolean shouldRender(World world, Orientation orientation, ItemStack stack, RayTraceResult trace);
    }

    @FunctionalInterface
    public interface OrientationGetter {
        Orientation get(EntityPlayer player, RayTraceResult trace, ItemStack stack);
    }

    public static Map<Item, Checker> eligibleItems = new IdentityHashMap<>();
    public static Map<Item, OrientationGetter> orientationGetters = new IdentityHashMap<>();

    public static boolean defaultChecker(World world, Orientation orientation, ItemStack stack, RayTraceResult trace) {
        return trace.sideHit != EnumFacing.UP;
    }

    public static Orientation defaultOrientationGetter(EntityPlayer player, RayTraceResult trace, ItemStack stack) {
        return SpaceUtils.getOrientation(trace.getBlockPos(), player, trace.sideHit, trace.hitVec.subtract(new Vec3d(trace.getBlockPos())));
    }

    public static void register(Item... i) {
        register(ArrowHighlightHandler::defaultChecker, ArrowHighlightHandler::defaultOrientationGetter, i);
    }

    public static void register(Checker check, OrientationGetter getter, Item... i) {
        if (eligibleItems.isEmpty() && i.length > 0) {
            MinecraftForge.EVENT_BUS.register(new ArrowHighlightHandler());
        }
        for (Item item : i) {
            eligibleItems.put(item, check);
            orientationGetters.put(item, getter);
        }
    }

    private void drawArrowHighlight(EntityPlayer player, RayTraceResult trace, Vec3d cameraPos, ItemStack stack, Checker check, OrientationGetter orientationGetter) {
        Orientation orientation = orientationGetter.get(player, trace, stack);

        if (orientation.top.getAxis() == trace.sideHit.getAxis() || !check.shouldRender(player.getEntityWorld(), orientation, stack, trace)) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
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
                if (!is.isEmpty() && eligibleItems.containsKey(is.getItem())) {
                    RayTraceResult mop = mc.objectMouseOver;
                    if (mop != null && mop.hitVec != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                        Entity rve = Minecraft.getMinecraft().getRenderViewEntity();
                        if (rve == null) {
                            rve = player;
                        }
                        drawArrowHighlight(player, mop, EntityUtils.interpolate(rve, event.getPartialTicks()), is, eligibleItems.get(is.getItem()), orientationGetters.get(is.getItem()));
                    }
                }
            }
        }
    }
}
