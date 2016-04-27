package pl.asie.charset.storage.locking;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * Created by asie on 4/27/16.
 */
public class LockEventHandler {
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        IBlockState state = event.getState();
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tile = event.getWorld().getTileEntity(event.getPos());
            if (tile instanceof ILockableContainer && ((ILockableContainer) tile).isLocked()) {
                LockCode code = ((ILockableContainer) tile).getLockCode();
                if (code.getLock().startsWith("charset:")) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
