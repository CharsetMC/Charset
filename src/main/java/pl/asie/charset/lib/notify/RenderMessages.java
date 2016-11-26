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

package pl.asie.charset.lib.notify;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
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
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RenderMessages extends RenderMessagesProxy {
    static final List<ClientMessage> messages = Collections.synchronizedList(new ArrayList<ClientMessage>());
    
    {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new PointCommand());
    }

    @Override
    public void addMessage(Object locus, ItemStack item, String format, String... args) {
        synchronized (messages) {
            addMessage0(locus, item, format, args);
        }
    }

    private void addMessage0(Object locus, ItemStack item, String format, String... args) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || player.world == null) {
            return;
        }
        ClientMessage msg = new ClientMessage(player.world, locus, item, format, args);
        if (msg.style.contains(Style.CLEAR)) {
            messages.clear();
            if (msg.msg == null || msg.msg.equals("")) return;
        }
        if (msg.style.contains(Style.UPDATE) || msg.style.contains(Style.UPDATE_SAME_ITEM)) {
            updateMessage(msg);
            return;
        }
        
        boolean force_position = msg.style.contains(Style.FORCE);
        
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
        if (msg.msg == null || msg.msg.trim().length() == 0) {
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
            if (!update.style.contains(Style.UPDATE_SAME_ITEM)) {
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
        double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * (double) event.getPartialTicks();
        double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * (double) event.getPartialTicks();
        double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * (double) event.getPartialTicks();
        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_BLEND);
        GlStateManager.translate(-cx, -cy, -cz);

        GlStateManager.depthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
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
            if (!m.style.contains(Style.DRAWFAR)) {
                Vec3d pos = m.getPosition(event.getPartialTicks());
                double dist = camera.getDistance(pos.xCoord, pos.yCoord, pos.zCoord);
                if (dist > 8) {
                    continue;
                }
            }
            GlStateManager.disableLighting();
            float lifeLeft = (m.lifeTime - timeExisted)/1000F;
            float opacity = 1F;
            if (lifeLeft < 1) {
                opacity = lifeLeft / 1F;
            }
            opacity = (float) Math.sin(opacity);
            if (opacity > 0.12) {
                renderMessage(m, event.getPartialTicks(), opacity, cx, cy, cz);
            }
        }
        GlStateManager.enableLighting();
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
        //RenderUtil.checkGLError("Notification render error!"); TODO
    }

    private RenderItem renderItem = null;

    private void renderMessage(ClientMessage m, float partial, float opacity, double cx, double cy, double cz) {
        int width = 0;
        String[] lines = m.msg.split("\n");
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
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
        
        float x = (float) vec.xCoord;
        float y = (float) vec.yCoord;
        float z = (float) vec.zCoord;
        if (m.style.contains(Style.SCALE_SIZE)) {
            double dx = x - cx;
            double dy = y - cy;
            double dz = z - cz;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            scaling *= Math.sqrt(dist);
        }
        
        NotificationCoord co = m.asCoord();
        if (co != null && !m.position_important) {
            BlockPos pos = co.getPos();
            IBlockState bs = co.getWorld().getBlockState(pos);
            AxisAlignedBB bb = bs.getCollisionBoundingBox(co.getWorld(), pos);
            if (bb != null) {
                y += bb.maxY - bb.minY;
            } else {
                y += 0.5F;
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
            float c = 0.0F;
            GlStateManager.disableTexture2D();
            GlStateManager.color(c, c, c, Math.min(opacity, 0.2F));
            double Z = 0.001D;
            // TODO: Why didn't the tessellator work?
            // TODO: Use 2 tessellator + 2 draw calls to do all notice rendering
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(-halfWidth - 1, -1, Z);
            GL11.glVertex3d(-halfWidth - 1, 8 + lineHeight, Z);
            GL11.glVertex3d(halfWidth + 1 + item_add, 8 + lineHeight, Z);
            GL11.glVertex3d(halfWidth + 1 + item_add, -1, Z);
            GL11.glEnd();
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
                //GL11.glColor4f(opacity, opacity, opacity, opacity);
                // :| Friggin' resets the transparency don't it...
                GlStateManager.translate(0, -centeringOffset, 0);

                GlStateManager.translate((float) (halfWidth + 4), -lineCount/2, 0);
                renderItem.zLevel -= 100; // Undoes the effects of setupGuiTransform
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
                renderItem.renderItemIntoGUI(m.item, 0, 0);
                GL11.glPopAttrib();
                renderItem.zLevel += 100;
            }
        }
        GlStateManager.popMatrix();

    }
    
    @Override
    public void onscreen(String message, String[] formatArgs) {
        Minecraft mc = Minecraft.getMinecraft();
        Object targs[] = new Object[formatArgs.length];
        for (int i = 0; i < formatArgs.length; i++) {
            targs[i] = net.minecraft.util.text.translation.I18n.translateToLocal(formatArgs[i]);
        }
        String msg = I18n.format(message, targs);
        // TODO 1.11 check
        mc.ingameGUI.setRecordPlayingMessage(msg);
    }
    
    @Override
    public void replaceable(ITextComponent msg, int msgKey) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(msg, msgKey);
    }
}
