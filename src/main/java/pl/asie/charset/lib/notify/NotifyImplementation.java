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
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.notify.component.NotificationComponent;
import pl.asie.charset.lib.utils.EntityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

public class NotifyImplementation {
    @CharsetModule.SidedProxy(clientSide = "pl.asie.charset.lib.notify.NotifyProxyClient", serverSide = "pl.asie.charset.lib.notify.NotifyProxy")
    public static NotifyProxy proxy;
    public static NotifyImplementation instance;

    public static void init() {
        NotifyImplementation.instance = new NotifyImplementation();
        MinecraftForge.EVENT_BUS.register(NotifyImplementation.instance);
        proxy.init();
    }

    public void registerServerCommands(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMutter());
    }
    
    void doSend(EntityPlayer player, Object where, World world, EnumSet<NoticeStyle> style, NotificationComponent message) {
        if (where == null) {
            return;
        }
        int baseRange = 32;
        if (player != null && EntityUtils.isPlayerFake(player)) {
            player = null;
        }
        if ((player != null && player.world.isRemote) || (world != null && world.isRemote)) {
            proxy.addMessage(where, style, message);
        } else {
            TargetPoint target = null;
            if (player == null) {
                final int range = style.contains(NoticeStyle.DRAWFAR) ? (baseRange * 4) : baseRange;
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
                    x = (int) vec.x;
                    y = (int) vec.y;
                    z = (int) vec.z;
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
            if (player != null) {
                CharsetLib.packet.sendTo(PacketNotification.createNotify(where, style, message), player);
            } else {
                CharsetLib.packet.sendToAllAround(PacketNotification.createNotify(where, style, message), target);
            }
        }
    }
    
    public static void recieve(EntityPlayer player, Object where, Collection<NoticeStyle> style, NotificationComponent msg) {
        if (where == null) {
            return;
        }
        proxy.addMessage(where, style, msg);
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
    
    void doSendOnscreenMessage(EntityPlayer player, Collection<NoticeStyle> styles, NotificationComponent msg) {
        if (player.world.isRemote) {
            proxy.onscreen(styles, msg);
        } else {
            CharsetLib.packet.sendTo(PacketNotification.createOnscreen(styles, msg), player);
        }
    }
}
