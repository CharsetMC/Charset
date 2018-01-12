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

package pl.asie.charset.lib.notify;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.utils.EntityUtils;

import java.lang.reflect.Field;
import java.util.*;

public class NotifyProxyClient extends NotifyProxy {
    static final List<ClientMessage> messages = Collections.synchronizedList(new ArrayList<ClientMessage>());

    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandPoint());
    }

    @Override
    public void addMessage(Object locus, ItemStack item, Collection<NoticeStyle> style, ITextComponent msg) {
        synchronized (messages) {
            addMessage0(locus, item, style, msg);
        }
    }

    private void addMessage0(Object locus, ItemStack item, Collection<NoticeStyle> style, ITextComponent cmsg) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || player.world == null) {
            return;
        }
        ClientMessage msg = new ClientMessage(player.world, locus, item, style, cmsg);
        if (msg.style.contains(NoticeStyle.CLEAR)) {
            messages.clear();
            if (msg.msg == null || msg.msg.equals("")) return;
        }
        if (msg.style.contains(NoticeStyle.UPDATE) || msg.style.contains(NoticeStyle.UPDATE_SAME_ITEM)) {
            updateMessage(msg);
            return;
        }
        
        boolean force_position = msg.style.contains(NoticeStyle.FORCE);
        
        if (messages.size() > 4 && !force_position) {
            messages.remove(0);
        }
        Vec3d testPos = msg.getPosition(0);
        if (testPos == null) {
            return;
        }
        for (ClientMessage m : messages) {
            if (m.getPosition(0).distanceTo(testPos) < 1.05 && !force_position) {
                m.creationTime = 0;
            }
        }
        if (msg.msg == null || msg.msgRendered.trim().length() == 0) {
            if (!(msg.show_item && msg.item != null)) {
                return;
            }
        }
        messages.add(msg);
    }

    private void updateMessage(ClientMessage update) {
        synchronized (messages) {
            updateMessage0(update);
        }
    }

    private void updateMessage0(ClientMessage update) {
        for (ClientMessage msg : messages) {
            if (!msg.locus.equals(update.locus)) {
                continue;
            }
            if (!update.style.contains(NoticeStyle.UPDATE_SAME_ITEM)) {
                msg.item = update.item;
            }
            msg.msg = update.msg;
            return;
        }
        // Otherwise it's an UPDATE to a non-existing message.
        // Presumably it's to a message that's died already.
    }

    @SubscribeEvent
    public void renderMessages(RenderWorldLastEvent event) {
        synchronized (messages) {
            renderMessages0(event);
        }
    }
    
    void renderMessages0(RenderWorldLastEvent event) {
        World w = Minecraft.getMinecraft().world;
        if (w == null) {
            return;
        }
        if (messages.size() == 0) {
            return;
        }
        // TODO
        // RenderUtil.checkGLError("A mod has a rendering error");
        Iterator<ClientMessage> it = messages.iterator();
        long approximateNow = System.currentTimeMillis();
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        Vec3d cameraPos = EntityUtils.interpolate(camera, event.getPartialTicks());
        GlStateManager.pushMatrix();
        GlStateManager.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 1);

        if (renderItem == null) {
            renderItem = Minecraft.getMinecraft().getRenderItem();
        }

        while (it.hasNext()) {
            ClientMessage m = it.next();
            long timeExisted = approximateNow - m.creationTime;
            if (timeExisted > m.lifeTime || m.world != w || !m.stillValid()) {
                it.remove();
                continue;
            }
            if (!m.style.contains(NoticeStyle.DRAWFAR)) {
                Vec3d pos = m.getPosition(event.getPartialTicks());
                double dist = camera.getDistance(pos.x, pos.y, pos.z);
                if (dist > 8) {
                    continue;
                }
            }
            float lifeLeft = (m.lifeTime - timeExisted)/1000F;
            float opacity = 1F;
            if (lifeLeft < 1) {
                opacity = lifeLeft / 1F;
            }
            opacity = (float) Math.sin(opacity);
            if (opacity > 0.12) {
                renderMessage(m, event.getPartialTicks(), opacity, cameraPos);
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.popMatrix();
        //RenderUtil.checkGLError("Notification render error!"); TODO
    }

    private RenderItem renderItem = null;

    private void renderMessage(ClientMessage m, float partial, float opacity, Vec3d c) {
        int width = 0;
        String[] lines = m.msgRendered.split("\n");

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();

        for (String line : lines) {
            width = Math.max(width, fr.getStringWidth(line));
        }
        width += 2;
        int halfWidth = width / 2;

        float scaling = 1.6F / 60F;
        scaling *= 2F / 3F;
        GlStateManager.pushMatrix();

        int lineCount = lines.length;
        float centeringOffset = 0;
        if (m.show_item) {
            if (lineCount == 1) {
                centeringOffset = 5F;
            }
            lineCount = Math.max(2, lineCount);
        }

        Vec3d vec = m.getPosition(partial);
        
        float x = (float) vec.x;
        float y = (float) vec.y;
        float z = (float) vec.z;
        if (m.style.contains(NoticeStyle.SCALE_SIZE)) {
            double dx = x - c.x;
            double dy = y - c.y;
            double dz = z - c.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            scaling *= Math.sqrt(dist);
        }

        NotificationCoord co = m.asCoord();
        if (co != null && !m.position_important) {
            BlockPos pos = co.getPos();
            IBlockState bs = co.getWorld().getBlockState(pos);
            AxisAlignedBB bb = bs.getCollisionBoundingBox(co.getWorld(), pos);
            if (bb != null) {
                y = (float) Math.max(y, pos.getY() + bb.maxY);
            } else {
                y = (float) Math.max(y, pos.getY() + 0.5f);
            }
        }
        GlStateManager.translate(x + 0.5F, y, z + 0.5F);
        Minecraft mc = Minecraft.getMinecraft();
        float pvx = mc.getRenderManager().playerViewX;
        float pvy = -mc.getRenderManager().playerViewY;
        if (mc.gameSettings.thirdPersonView == 2) {
            pvx = -pvx;
        }
        GlStateManager.rotate(pvy, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pvx, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scaling, -scaling, scaling);
        GlStateManager.translate(0, -10 * lineCount, 0);
        
        {
            int lineHeight = (lineCount - 1) * 10;

            double item_add = 0;
            if (m.show_item) {
                item_add += 24;
            }
            float col = 0.0F;
            GlStateManager.disableTexture2D();
            GlStateManager.color(col, col, col, Math.min(opacity, 0.2F));
            double Z = 0.001D;
            // TODO: Use 2 tessellator + 2 draw calls to do all notice rendering

            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            worldrenderer.pos(-halfWidth - 1, -1, Z).endVertex();
            worldrenderer.pos(-halfWidth - 1, 8 + lineHeight, Z).endVertex();
            worldrenderer.pos(halfWidth + 1 + item_add, 8 + lineHeight, Z).endVertex();
            worldrenderer.pos(halfWidth + 1 + item_add, -1, Z).endVertex();

            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        {
            int i = 0;
            int B = (int) (0xFF * Math.min(1, 0.5F + opacity));
            int color = (B << 16) + (B << 8) + B + ((int) (0xFF*opacity) << 24);
            GlStateManager.translate(0, centeringOffset, 0);
            for (String line : lines) {
                fr.drawString(line, -fr.getStringWidth(line) / 2, 10 * i, color);
                i++;
            }
        }
        {
            if (m.show_item) {
                // TODO: Add transparency support
                GlStateManager.translate(0, -centeringOffset, 0);

                GlStateManager.translate((float) (halfWidth + 4), -lineCount/2, 0);
                renderItem.zLevel -= 100; // Undoes the effects of setupGuiTransform
                renderItem.renderItemIntoGUI(m.item, 0, 0);
                renderItem.zLevel += 100;
            }
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void onscreen(Collection<NoticeStyle> style, ITextComponent msg) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.ingameGUI.setOverlayMessage(msg, false);
        // TODO: Implement some NoticeStyles (such as LONG)
    }
}
