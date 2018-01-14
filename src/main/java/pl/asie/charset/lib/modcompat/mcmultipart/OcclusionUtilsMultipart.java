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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.world.IMultipartBlockAccess;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.utils.OcclusionUtils;

import java.util.Collection;

public class OcclusionUtilsMultipart extends OcclusionUtils {
    public OcclusionUtilsMultipart() {
    }

    @Override
    public boolean intersects(Collection<AxisAlignedBB> boxes1, IBlockAccess world, BlockPos pos) {
        if (world instanceof IMultipartBlockAccess) {
            IPartInfo info = ((IMultipartBlockAccess) world).getPartInfo();
            for (IPartInfo info2 : info.getContainer().getParts().values()) {
                if (info2 != info && intersects(boxes1, info2.getPart().getOcclusionBoxes(info2))) {
                    return true;
                }
            }

            return false;
        } else {
            return super.intersects(boxes1, world, pos);
        }
    }
}
