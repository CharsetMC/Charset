package pl.asie.charset.misc.shelf.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.misc.shelf.BlockShelf;
import pl.asie.charset.misc.shelf.CharsetMiscShelf;
import pl.asie.charset.pipes.CharsetPipes;

public class MultipartShelf implements IMultipart {
    @Override
    public Block getBlock() {
        return CharsetMiscShelf.shelfBlock;
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        return EnumFaceSlot.fromFace(BlockShelf.getFacePlaced(facing, hitX, hitY, hitZ, placer));
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        return EnumFaceSlot.fromFace(BlockShelf.getFace(state.getValue(Properties.FACING4), state.getValue(BlockShelf.BACK)));
    }
}
