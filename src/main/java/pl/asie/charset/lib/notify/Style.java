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

import net.minecraft.tileentity.TileEntity;

public enum Style {
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