package pl.asie.charset.misc.scaffold.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumSlotAccess;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.misc.scaffold.CharsetMiscScaffold;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MultipartScaffold implements IMultipart {
    private static final AxisAlignedBB OCCLUSION_BOX_TOP = new AxisAlignedBB(0, 1 - 0.0625, 0, 1, 1, 1);

    @Override
    public List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
        return Collections.singletonList(OCCLUSION_BOX_TOP);
    }

    @Override
    public Block getBlock() {
        return CharsetMiscScaffold.scaffoldBlock;
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        // TODO: Await MCMultiPart update to add custom slot
        return EnumCenterSlot.CENTER;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return EnumCenterSlot.CENTER;
    }
}
