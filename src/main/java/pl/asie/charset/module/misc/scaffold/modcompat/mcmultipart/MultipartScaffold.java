package pl.asie.charset.module.misc.scaffold.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MultipartScaffold implements IMultipart {
    public static class Slot implements IPartSlot {
        public static final Slot INSTANCE = new Slot();

        @Override
        public EnumSlotAccess getFaceAccess(EnumFacing face) {
            return EnumSlotAccess.MERGE;
        }

        @Override
        public int getFaceAccessPriority(EnumFacing face) {
            return 250;
        }

        @Override
        public EnumSlotAccess getEdgeAccess(EnumEdgeSlot edge, EnumFacing face) {
            return EnumSlotAccess.NONE;
        }

        @Override
        public int getEdgeAccessPriority(EnumEdgeSlot edge, EnumFacing face) {
            return 200;
        }

        @Nullable
        @Override
        public ResourceLocation getRegistryName() {
            return new ResourceLocation("charset:slot_scaffold");
        }
    }

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
        return Slot.INSTANCE;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return Slot.INSTANCE;
    }
}
