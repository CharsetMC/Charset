package pl.asie.charset.pipes;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemPipe extends Item {
    public ItemPipe() {
        setUnlocalizedName("charset.pipe");
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        PartPipe pipe = new PartPipe();

        if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(side);
        }

        if (stack.stackSize == 0)
        {
            return false;
        }
        else if (!worldIn.isRemote) {
            return MultipartHelper.addPartIfPossible(worldIn, pos, pipe);
        } else {
            return true;
        }
    }

}
