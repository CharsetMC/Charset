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

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;

class ClientMessage {
    World world;
    Object locus;
    ItemStack item;
    String msg;
    EnumSet<NoticeStyle> style;
    
    long creationTime;
    long lifeTime;
    boolean position_important = false;
    boolean show_item = false;
    
    static final int SHORT_TIME = 6, LONG_TIME = 11, VERY_LONG_TIME = 60;

    public ClientMessage(World world, Object locus, ItemStack item, String format, String... args) {
        this.world = world;
        this.locus = locus;
        this.item = item;
        this.msg = format;
        
        String[] parts = msg.split("\n", 2);
        style = NotifyImplementation.loadStyle(parts[0]);
        msg = parts[1];
        
        creationTime = System.currentTimeMillis();
        if (style.contains(NoticeStyle.LONG)) {
            lifeTime = 1000 * LONG_TIME;
        } else {
            lifeTime = 1000 * SHORT_TIME;
        }
        position_important = style.contains(NoticeStyle.EXACTPOSITION);
        show_item = style.contains(NoticeStyle.DRAWITEM) && !item.isEmpty();
        translate(args);
    }
    
    void translate(String... args) {
        msg = I18n.translateToLocal(msg);
        msg = msg.replace("\\n", "\n");
        
        String item_name = "null", item_info = "", item_info_newline = "";
        if (item != null) {
            item_name = item.getDisplayName();
            EntityPlayer player = Minecraft.getMinecraft().player;
            ArrayList<String> bits = new ArrayList<String>();
            try {
                item.getItem().addInformation(item, player.getEntityWorld(), bits, ITooltipFlag.TooltipFlags.NORMAL);
            } catch (Throwable t) {
                t.printStackTrace();
                bits.add("" + TextFormatting.RED + TextFormatting.BOLD + "ERROR");
            }
            boolean tail = false;
            for (String s : bits) {
                if (tail) {
                    item_info += "\n";
                }
                tail = true;
                item_info += s;
            }
            item_info_newline = "\n" + item_info;
        }

        String[] cp = new String[args.length + 3];
        for (int i = 0; i < args.length; i++) {
            cp[i] = I18n.translateToLocal(args[i]); // TODO
        }
        cp[args.length] = item_name;
        cp[args.length + 1] = item_info;
        cp[args.length + 2] = item_info_newline;
        msg = msg.replace("{ITEM_NAME}", "%" + (args.length + 1) + "$s");
        msg = msg.replace("{ITEM_INFOS}", "%" + (args.length + 2) + "$s");
        msg = msg.replace("{ITEM_INFOS_NEWLINE}", "%" + (args.length + 3) + "$s");

        msg = String.format(msg, (Object[]) cp);
    }
    
    static double interp(double old, double new_, float partial) {
        return old*(1 - partial) + new_*partial;
    }

    Vec3d getPosition(float partial) {
        if (locus instanceof Vec3d) {
            return (Vec3d) locus;
        }
        if (locus instanceof Entity) {
            if (locus instanceof EntityMinecart) {
                partial = 1; // Wtf?
            }
            Entity e = ((Entity) locus);
            double w = e.width * -1.5;
            double eye_height = 4.0 / 16.0;
            if (e instanceof EntityLiving) {
                eye_height += e.getEyeHeight();
            }
            final double x = interp(e.prevPosX, e.posX, partial) + w / 2;
            final double y = interp(e.prevPosY, e.posY, partial) + eye_height;
            final double z = interp(e.prevPosZ, e.posZ, partial) + w / 2;
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
}