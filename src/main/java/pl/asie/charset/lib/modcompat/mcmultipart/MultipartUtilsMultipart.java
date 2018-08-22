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

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.world.IMultipartBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.utils.MultipartUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class MultipartUtilsMultipart extends MultipartUtils {
    public MultipartUtilsMultipart() {
    }

    private static IPartInfo getPartInfo(RayTraceResult mouseOver) {
        if (mouseOver.hitInfo instanceof IPartInfo) {
            return (IPartInfo) mouseOver.hitInfo;
        }

        if (mouseOver.hitInfo instanceof RayTraceResult && mouseOver.hitInfo != mouseOver) {
            RayTraceResult result = (RayTraceResult) mouseOver.hitInfo;
            mouseOver.hitInfo = null; // prevent circular loops
            IPartInfo v = getPartInfo(result);
            mouseOver.hitInfo = result;
            return v;
        }

        return null;
    }

    @Override
    public ExtendedRayTraceResult getTrueResult(RayTraceResult result) {
        IPartInfo info = getPartInfo(result);
        if (info != null) {
            return new ExtendedRayTraceResult(result, info.getTile() != null ? info.getTile().getTileEntity() : null);
        } else {
            return super.getTrueResult(result);
        }
    }

    @Override
    public boolean intersects(Collection<AxisAlignedBB> boxes1, IBlockAccess world, BlockPos pos, Predicate<IBlockState> checkPredicate) {
        Optional<IMultipartContainer> ctr = MultipartHelper.getContainer(world, pos);

        if (ctr.isPresent()) {
            IPartInfo info = null;
            if (world instanceof IMultipartBlockAccess) {
                info = ((IMultipartBlockAccess) world).getPartInfo();
            }

            for (IPartInfo info2 : ctr.get().getParts().values()) {
                if (info2 != info && checkPredicate.test(info2.getState()) && intersects(boxes1, info2.getPart().getOcclusionBoxes(info2))) {
                    return true;
                }
            }

            return false;
        } else {
            return super.intersects(boxes1, world, pos, checkPredicate);
        }
    }
}
