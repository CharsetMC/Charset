/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.notify.component.NotificationComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

class ClientMessage {
    World world;
    Object locus;
    private NotificationComponent msg;
    String msgRendered;
    Collection<NoticeStyle> style;
    
    long creationTime;
    long lifeTime;
    boolean position_important = false;
    boolean show_item = false;
    
    static final int SHORT_TIME = 6, LONG_TIME = 11, VERY_LONG_TIME = 60;

    public ClientMessage(World world, Object locus, Collection<NoticeStyle> styles, NotificationComponent msg) {
        this.world = world;
        this.locus = locus;
        this.msg = msg;
        this.style = styles;
        
        creationTime = System.currentTimeMillis();
        if (style.contains(NoticeStyle.LONG)) {
            lifeTime = 1000 * LONG_TIME;
        } else {
            lifeTime = 1000 * SHORT_TIME;
        }
        position_important = style.contains(NoticeStyle.EXACTPOSITION);
        show_item = style.contains(NoticeStyle.DRAWITEM);
        translate();
    }

    void setMessage(NotificationComponent msg) {
        this.msg = msg;
        translate();
    }

    void translate() {
        msgRendered = msg.toString().replace("\\n", "\n");
    }
    
    static double interp(double old, double new_, float partial) {
        return old*(1 - partial) + new_*partial;
    }

    Vec3d getPosition(float partial) {
        if (locus instanceof Vec3d) {
            return (Vec3d) locus;
        }
        if (locus instanceof Entity) {
            Entity e = ((Entity) locus);
            double w = e.width * -1;
            final double x = interp(e.lastTickPosX, e.posX, partial) + w / 2;
            final double y = interp(e.lastTickPosY, e.posY, partial) + e.height;
            final double z = interp(e.lastTickPosZ, e.posZ, partial) + w / 2;
            return new Vec3d(x, y, z);
        }
        if (locus instanceof TileEntity) {
            TileEntity te = ((TileEntity) locus);
            return new Vec3d(te.getPos());
        }
        if (locus instanceof NotificationCoord) {
            return new Vec3d(((NotificationCoord) locus).getPos());
        }
        return null;
    }

    boolean stillValid() {
        if (locus instanceof Entity) {
            Entity e = ((Entity) locus);
            return !e.isDead;
        }
        if (locus instanceof TileEntity) {
            TileEntity te = ((TileEntity) locus);
            return !te.isInvalid();
        }
        return true;
    }

    public @Nullable NotificationCoord asCoord() {
        if (locus instanceof TileEntity) {
            TileEntity te = ((TileEntity) locus);
            return new NotificationCoord(te.getWorld(), te.getPos());
        }
        if (locus instanceof NotificationCoord) {
            return new NotificationCoord(((NotificationCoord) locus).getWorld(), ((NotificationCoord) locus).getPos());
        }
        return null;
    }

    public NotificationComponent getMessage() {
        return msg;
    }
}