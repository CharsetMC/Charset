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
