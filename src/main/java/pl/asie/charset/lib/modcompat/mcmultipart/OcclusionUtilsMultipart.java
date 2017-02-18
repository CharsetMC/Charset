package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.multipart.OcclusionHelper;
import mcmultipart.api.world.IMultipartBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.utils.OcclusionUtils;

import java.util.*;

public class OcclusionUtilsMultipart extends OcclusionUtils {
    public OcclusionUtilsMultipart() {
    }

    @Override
    public boolean intersects(Collection<AxisAlignedBB> boxes1, IBlockAccess world, BlockPos pos) {
        if (world instanceof IMultipartBlockAccess) {
            IPartInfo info = ((IMultipartBlockAccess) world).getPartInfo();
            for (IPartInfo info2 : info.getContainer().getParts().values()) {
                if (info2 != info && OcclusionHelper.testPartIntersection(info, info2)) {
                    return true;
                }
            }

            return false;
        } else {
            return super.intersects(boxes1, world, pos);
        }
    }
}
