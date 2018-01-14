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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

public class Notice {
    final Object where;
    private ITextComponent message;
    private ItemStack item = ItemStack.EMPTY;
    private EnumSet<NoticeStyle> style = EnumSet.noneOf(NoticeStyle.class);
    private INoticeUpdater updater;

    private boolean isUpdating = false;
    private int age = 0;
    private boolean changed = false;
    private boolean changedItem = false;
    private boolean addedToRecurList = false;
    EntityPlayer targetPlayer = null;
    World world;
    // NORELEASE: Support for ITextComponent?

    private static final String[] emptyStringArray = new String[0];

    /**
     * Creates an in-world notification message, which is sent using
     * {@link #sendTo(EntityPlayer)} or {@link #sendToAll()}. Additional options can be
     * provided using {@link #withItem(ItemStack)} m}, {@link #withStyle(NoticeStyle...)}, {@link #withWorld(World)},
     * and {@link #withUpdater(INoticeUpdater)}. <br>
     * <b>Remember to send the Notice!</b><br>
     * <code><pre>
     * Notice msg = new Notice(oldManEntity, "%s", "It's dangerous to go alone!\nTake this!");
     * msg.withItem(new ItemStack(Items.iron_sword)).sendTo(player); 
     * </pre></code>
     * 
     * @param where
     *            An {@link Entity}, {@link TileEntity}, {@link Vec3d} or {@link NotificationCoord}
     * @param message
     *            The message to be sent.
     */

    @CheckReturnValue
    public Notice(Object where, ITextComponent message) {
        this.where = where;
        this.message = message;
        if (where instanceof NotificationCoord) {
            world = ((NotificationCoord) where).getWorld();
        } else if (where instanceof Entity) {
            world = ((Entity) where).world;
        } else if (where instanceof TileEntity) {
            world = ((TileEntity) where).getWorld();
        }
    }
    
    /**
     * Creates a new Notice. The {@link INoticeUpdater} will be used to populate the initial message,
     * and will be called repeatedly until some amount of time passes. If the message changes,
     * then the clients will be updated.
     * 
     * <pre>
     * <code>
     * new Notice(somewhere, new INoticeUpdater() {
     *     void update(Notice msg) {
     *         msg.setMessage("%s", System.currentTimeMillis()/1000);
     *     }
     * }).sendTo(someone);
     * </code>
     * </pre>
     * 
     * @param where
     *            An {@link Entity}, {@link TileEntity}, {@link Vec3d} or {@link NotificationCoord}
     * @param updater
     *             The {@link INoticeUpdater} object.
     * 
     */
    @CheckReturnValue
    public Notice(Object where, INoticeUpdater updater) {
        this.where = where;
        withUpdater(updater);
        updater.update(this);
    }
    
    /**
     * <p>
     * Sets a single item to be sent along with the message. It can be used in
     * two ways.
     * </p>
     * 
     * <p>
     * If {@link #withStyle(NoticeStyle...)} is used, then the item will
     * be drawn in the notification.
     * </p>
     * 
     * <p>
     * The item's name can be inserted in the message's text using the format
     * codes <code>{ITEM_NAME}</code>, <code>{ITEM_INFOS}</code>, or <code>{ITEM_INFOS_NEWLINE}</code>.
     * </p>
     * <p>
     * <code>{ITEM_NAME}</code>
     * will be replaced with the name of the item, and is gotten by calling
     * {@link ItemStack#getDisplayName}. <code>{ITEM_INFOS}</code> and <code>{ITEM_INFOS_NEWLINE</code> are
     * gotten via Item.addInformation. ITEM_INFOS_NEWLINE is prefixed
     * with a newline, unless the information list is empty.
     * </p>
     */
    @CheckReturnValue
    public Notice withItem(ItemStack item) {
        if (isUpdating && !changed) {
            cmpIs(this.item, item);
            changedItem |= changed;
        }
        this.item = item.isEmpty() ? ItemStack.EMPTY : item.copy();
        if (this.item.getCount() > this.item.getMaxStackSize())
            this.item.setCount(item.getMaxStackSize());
        return this;
    }

    /**
     * Sets the {@link NoticeStyle}s for the message. See {@link NoticeStyle} for details.
     */
    @CheckReturnValue
    public Notice withStyle(NoticeStyle... styles) {
        boolean addedStyle = false;
        for (NoticeStyle s : styles) {
            addedStyle |= style.add(s);
        }
        if (addedStyle && isUpdating) {
            this.changed = true;
        }
        return this;
    }

    public Notice withStyle(Collection<NoticeStyle> styles) {
        boolean addedStyle = false;
        for (NoticeStyle s : styles) {
            addedStyle |= style.add(s);
        }
        if (addedStyle && isUpdating) {
            this.changed = true;
        }
        return this;
    }


    /**
     * Sets the world of the Notice. (This is only needed for sending a
     * notification at a Vec3 position to everyone;)
     */
    @CheckReturnValue
    public Notice withWorld(World world) {
        this.world = world;
        return this;
    }

    /**
     * Schedules a recurring notification.
     * <code>{@link INoticeUpdater#update}(this)</code> will be called until no
     * longer necessary.
     */
    @CheckReturnValue
    public Notice withUpdater(INoticeUpdater updater) {
        this.updater = updater;
        return this;
    }
    
    /**
     * Changes the message. This goes with the {@link INoticeUpdater} constructor.
     */
    public Notice setMessage(ITextComponent newMessage) {
        cmp(this.message, newMessage);
        this.message = newMessage;
        return this;
    }

    private void cmp(Object a, Object b) {
        if (a == b) return;
        if (a != null && b != null) {
            changed |= !a.equals(b);
        } else {
            changed = true;
        }
    }
    
    private void cmpIs(ItemStack a, ItemStack b) {
        changed |= ItemUtils.equalsMeta(a, b);
    }

    int getLifetime() {
        if (style.contains(NoticeStyle.VERY_LONG)) return ClientMessage.VERY_LONG_TIME;
        if (style.contains(NoticeStyle.LONG)) return ClientMessage.LONG_TIME;
        return ClientMessage.SHORT_TIME;
    }
    
    boolean isInvalid() {
        int maxAge = 20 * getLifetime();
        if (age++ > maxAge) {
            return true;
        }
        if (where instanceof Entity) {
            Entity ent = (Entity) where;
            if (ent.isDead) {
                return false;
            }
        } else if (where instanceof TileEntity) {
            TileEntity te = (TileEntity) where;
            if (te.isInvalid()) {
                return false;
            }
        } else if (where instanceof NotificationCoord) {
            NotificationCoord coord = (NotificationCoord) where;
            if (!coord.getWorld().isBlockLoaded(coord.getPos())) {
                return false;
            }
        } else if (where instanceof Vec3d && world != null) {
            Vec3d vec = (Vec3d) where;
            if (!world.isBlockLoaded(new BlockPos(vec))) {
                return false;
            }
        }
        if (targetPlayer != null) {
            return targetPlayer.isDead;
        }
        return false;

    }
    
    /**
     * Dispatches the Notice to the player. If the player is null, then all
     * players in the world will see it.
     */
    public void sendTo(EntityPlayer player) {
        if (isUpdating) {
            // In this case, it is our responsibility. Shouldn't be called.
            return;
        }
        if (world == null && player != null) {
            world = player.world;
        }
        NotifyImplementation.instance.doSend(player, where, world, style, item, message);
        changed = false;
        changedItem = false;
        if (updater != null && !addedToRecurList) {
            NotifyImplementation.instance.addRecuringNotification(this);
            targetPlayer = player;
            addedToRecurList = true;
        }
    }

    /**
     * Sends the Notice to everyone in the world.
     * @see Notice#sendTo
     */
    public void sendToAll() {
        sendTo(null);
    }

    /**
     * Erases all Notifications a player has.
     */
    public static void clear(EntityPlayer player) {
        NotificationCoord at = new NotificationCoord(player.world, new BlockPos(player));
        NotifyImplementation.instance.doSend(player, at, player.world, EnumSet.of(NoticeStyle.CLEAR), null, new TextComponentString(""));
    }

    /**
     * Sends an on-screen message, using Vanilla's mechanism for displaying the
     * color's "Press SHIFT to dismount" message.
     * (Unfortunately it kind of looks like crap because the text isn't shadowed. Oh well.)
     * 
     * @param player
     *            The player to be notified
     */
    public static void onscreen(EntityPlayer player, Collection<NoticeStyle> styles, ITextComponent msg) {
        NotifyImplementation.instance.doSendOnscreenMessage(player, styles, msg);
    }

    public static void title(EntityPlayer player, String title, String subtitle) {
        // NORELEASE: Implement. Just needs a little packet?
        onscreen(player, Collections.emptySet(), new TextComponentString(title + "\n" + subtitle));
    }

    public static void title(EntityPlayer player, String title) {
        onscreen(player, Collections.emptySet(), new TextComponentString(title));
    }

    boolean updateNotice() {
        if (updater == null)
            return false;
        if (targetPlayer != null && targetPlayer.isDead)
            return false;
        if (isUpdating)
            return false;
        isUpdating = true;
        updater.update(this);
        isUpdating = false;
        if (changed) {
            if (changedItem) {
                style.add(NoticeStyle.UPDATE);
                sendTo(targetPlayer);
                style.remove(NoticeStyle.UPDATE);
            } else {
                style.add(NoticeStyle.UPDATE_SAME_ITEM);
                sendTo(targetPlayer);
                style.remove(NoticeStyle.UPDATE_SAME_ITEM);
            }
            changed = changedItem = false;
        }
        return true;
    }

    public void cancel() {
        age = getLifetime();
    }

}
