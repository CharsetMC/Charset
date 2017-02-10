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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.CharsetLib;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

public class NotifyImplementation {
    @SidedProxy(modId = "charset", clientSide = "pl.asie.charset.lib.notify.NotifyProxyClient", serverSide = "pl.asie.charset.lib.notify.NotifyProxy")
    public static NotifyProxy proxy;
    public static NotifyImplementation instance;
    
    public static void init() {
        NotifyImplementation.instance = new NotifyImplementation();
        MinecraftForge.EVENT_BUS.register(NotifyImplementation.instance);
    }

    public void registerServerCommands(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMutter());
    }
    
    void doSend(EntityPlayer player, Object where, World world, EnumSet<NoticeStyle> style, ItemStack item, String format, String[] args) {
        if (where == null) {
            return;
        }
        if (player instanceof FakePlayer) {
            return;
        }
        format = styleMessage(style, format);
        if ((player != null && player.world.isRemote) || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            proxy.addMessage(where, item, format, args);
        } else {
            TargetPoint target = null;
            if (player == null) {
                final int range = style.contains(NoticeStyle.DRAWFAR) ? 128 : 32;
                int x = 0, y = 0, z = 0;
                boolean failed = false;
                BlockPos pos = null;
                if (where instanceof NotificationCoord) {
                    NotificationCoord c = (NotificationCoord) where;
                    pos = c.getPos();
                    world = c.getWorld();
                } else if (where instanceof TileEntity) {
                    TileEntity te = (TileEntity) where;
                    world = te.getWorld();
                    pos = te.getPos();
                } else if (where instanceof Entity) {
                    Entity ent = (Entity) where;
                    world = ent.world;
                    x = (int) ent.posX;
                    y = (int) ent.posY;
                    z = (int) ent.posZ;
                } else if (where instanceof Vec3d) {
                    Vec3d vec = (Vec3d) where;
                    x = (int) vec.xCoord;
                    y = (int) vec.yCoord;
                    z = (int) vec.zCoord;
                } else if (where instanceof BlockPos) {
                    pos = (BlockPos) where;
                } else {
                    failed = true;
                }
                if (pos != null) {
                    x = pos.getX();
                    y = pos.getY();
                    z = pos.getZ();
                }
                if (world != null && !failed) {
                    int dimension = world.provider.getDimension();
                    target = new TargetPoint(dimension, x, y, z, range);
                }
            }
            if (args == null) args = new String[0];
            if (player != null) {
                CharsetLib.packet.sendTo(PacketNotification.createNotify(where, item, format, args), player);
            } else {
                CharsetLib.packet.sendToAllAround(PacketNotification.createNotify(where, item, format, args), target);
            }
        }
    }
    
    public static void recieve(EntityPlayer player, Object where, ItemStack item, String styledFormat, String[] args) {
        if (where == null) {
            return;
        }
        proxy.addMessage(where, item, styledFormat, args);
    }
    
    String styleMessage(EnumSet<NoticeStyle> style, String format) {
        if (style == null) {
            return "\n" + format;
        }
        String prefix = "";
        String sep = "";
        for (NoticeStyle s : style) {
            prefix += sep + s.toString();
            sep = " ";
        }
        return prefix + "\n" + format;
    }
    
    static EnumSet<NoticeStyle> loadStyle(String firstLine) {
        EnumSet<NoticeStyle> ret = EnumSet.noneOf(NoticeStyle.class);
        for (String s : firstLine.split(" ")) {
            try {
                ret.add(NoticeStyle.valueOf(s));
            } catch (IllegalArgumentException e) {}
        }
        return ret;
    }
    
    private static final ArrayList<Notice> recuring_notifications = new ArrayList<Notice>();
    
    @SubscribeEvent
    public void updateRecuringNotifications(ServerTickEvent event) {
        if (event.phase != Phase.END) return;
        synchronized (recuring_notifications) {
            Iterator<Notice> iterator = recuring_notifications.iterator();
            while (iterator.hasNext()) {
                Notice rn = iterator.next();
                if (rn.isInvalid() || !rn.updateNotice()) {
                    iterator.remove();
                }
            }
        }
    }
    
    void addRecuringNotification(Notice newRN) {
        synchronized (recuring_notifications) {
            Iterator<Notice> iterator = recuring_notifications.iterator();
            while (iterator.hasNext()) {
                Notice rn = iterator.next();
                if (rn.where.equals(newRN.where) && (newRN.targetPlayer == null || newRN.targetPlayer == rn.targetPlayer)) {
                    iterator.remove();
                }
            }
            recuring_notifications.add(newRN);
        }
    }
    
    void doSendOnscreenMessage(EntityPlayer player, String message, String[] formatArgs) {
        if (player.world.isRemote) {
            proxy.onscreen(message, formatArgs);
        } else {
            CharsetLib.packet.sendTo(PacketNotification.createOnscreen(message, formatArgs), player);
        }
    }
    
    void sendReplacableChatMessage(EntityPlayer player, ITextComponent msg, int msgKey) {
        if (player.world.isRemote) {
            proxy.replaceable(msg, msgKey);
        } else {
            CharsetLib.packet.sendTo(PacketNotification.createReplaceable(msg, msgKey), player);
        }
    }
}
