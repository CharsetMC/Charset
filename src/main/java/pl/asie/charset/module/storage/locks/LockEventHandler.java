/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.storage.locks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LockEventHandler {
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        IBlockState state = event.getState();
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tile = event.getWorld().getTileEntity(event.getPos());
            if (tile instanceof ILockableContainer && ((ILockableContainer) tile).isLocked()) {
                LockCode code = ((ILockableContainer) tile).getLockCode();
                if (code.getLock().startsWith("charset:")) {
                    /* if (event.getWorld().getEntitiesWithinAABB(EntityLock.class,
                            new AxisAlignedBB(event.getPos()).expandXyz(0.25f)).size() > 0) {
                        event.setCanceled(true);
                    } */
                    event.setCanceled(true);
                }
            }
        }
    }
}
