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

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class NotificationCoord {
    final World w;
    final BlockPos pos;

    public NotificationCoord(World w, BlockPos pos) {
        this.w = w;
        this.pos = pos;
    }
    
    public World getWorld() { return w; }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotificationCoord)) return false;
        NotificationCoord o = (NotificationCoord) obj;
        return w == o.w && pos.equals(o.pos);
    }

    public static NotificationCoord fromMop(World world, RayTraceResult mop) {
        return new NotificationCoord(world, mop.getBlockPos());
    }
}
