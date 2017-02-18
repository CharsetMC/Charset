package pl.asie.charset.pipes.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.pipes.CharsetPipes;
import pl.asie.charset.pipes.PipeUtils;
import pl.asie.charset.pipes.pipe.BlockPipe;
import pl.asie.charset.pipes.pipe.TilePipe;

import java.util.Collections;
import java.util.List;

public class MultipartPipe implements IMultipart {
    @Override
    public Block getBlock() {
        return CharsetPipes.blockPipe;
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public void onPartChanged(IPartInfo part, IPartInfo otherPart) {
        TilePipe pipe = PipeUtils.getPipe(part.getTile().getTileEntity());
        if (pipe != null) {
            pipe.scheduleFullNeighborUpdate();
        }
    }
}
