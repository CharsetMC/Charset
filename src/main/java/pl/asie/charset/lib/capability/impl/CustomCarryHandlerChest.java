package pl.asie.charset.lib.capability.impl;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.carry.CustomCarryHandler;
import pl.asie.charset.api.carry.ICarryHandler;

public class CustomCarryHandlerChest extends CustomCarryHandler {
    public CustomCarryHandlerChest(ICarryHandler handler) {
        super(handler);
    }

    @Override
    public void onPlace(World world, BlockPos pos) {
        super.onPlace(world, pos);

        for (EnumFacing facing1 : EnumFacing.HORIZONTALS) {
            if (world.getBlockState(pos.offset(facing1)).getBlock() instanceof BlockChest) {
                // FIXME: Double chests need this (#137)
                owner.getState().getBlock().onBlockPlacedBy(world, pos, owner.getState(), (EntityLivingBase) owner.getCarrier(), ItemStack.EMPTY);
                break;
            }
        }
    }
}
