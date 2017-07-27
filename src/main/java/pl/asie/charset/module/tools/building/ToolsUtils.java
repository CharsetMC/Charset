package pl.asie.charset.module.tools.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ToolsUtils {
    private ToolsUtils() {

    }

    public static ActionResult<ItemStack> placeBlockOrRollback(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos) {
        ItemStack oldStack = stack.copy();
        ItemStack heldItem = playerIn.getHeldItem(EnumHand.MAIN_HAND);

        // Take a snapshot
        IBlockState state = worldIn.getBlockState(pos);
        NBTTagCompound nbtTile = null;
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null) {
                nbtTile = tileEntity.writeToNBT(new NBTTagCompound());
            }
        }

        worldIn.setBlockToAir(pos);

        playerIn.setHeldItem(EnumHand.MAIN_HAND, stack);
        EnumActionResult result1 = stack.onItemUse(
                playerIn, worldIn, pos, EnumHand.MAIN_HAND, EnumFacing.UP,
                0, 0, 0
        );

        ItemStack placedItem = playerIn.getHeldItem(EnumHand.MAIN_HAND);
        playerIn.setHeldItem(EnumHand.MAIN_HAND, heldItem);

        if (result1 == EnumActionResult.SUCCESS && !worldIn.isAirBlock(pos)) {
            // Hooray!
            return new ActionResult<>(EnumActionResult.SUCCESS, placedItem);
        } else {
            // Rollback...
            worldIn.setBlockState(pos, state);
            if (nbtTile != null) {
                worldIn.setTileEntity(pos, TileEntity.create(worldIn, nbtTile));
            }
            return new ActionResult<>(EnumActionResult.FAIL, oldStack);
        }
    }
}
