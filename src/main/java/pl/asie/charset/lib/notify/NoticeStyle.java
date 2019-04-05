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

package pl.asie.charset.lib.notify;

import net.minecraft.tileentity.TileEntity;

public enum NoticeStyle {
    /**
     * This message will not cause other nearby messages to be removed
     */
    FORCE,
    /**
     * This message will stay around for an extra 5 seconds
     */
    LONG,
    /**
     * This message will cause all messages to be removed (including itself)
     */
    CLEAR,
    /**
     * If the message is for a {@link NotificationCoord} or {@link TileEntity}, then it will not take the block's bounding box into consideration
     * (Other targets don't consider bounding boxes)
     */
    EXACTPOSITION,
    /**
     * This message will have its item drawn on the right side.
     */
    DRAWITEM,
    /**
     * Normally messages do not render if they're more than a few blocks away. This causes the message to always render.
     */
    DRAWFAR,
    /**
     * This will update the message at the target location, if there is one, without changing the despawn time.
     * {@link Notice.withUpdater} is preferable to using this manually.
     */
    UPDATE,
    /**
     * This will update the message at the target location, if there is one, without changing the despawn time.
     * The original item will not be changed.
     * {@link Notice.withUpdater} is preferable to using this manually.
     */
    UPDATE_SAME_ITEM,
    /**
     * This will enlarge messages far away from the camera so that they occupy about the same area on the screen as nearby messages.
     * You will likely want to pass in DRAWFAR with this one.
     */
    SCALE_SIZE,
    /**
     * The message will stay around for up to 60 seconds.
     */
    VERY_LONG
}