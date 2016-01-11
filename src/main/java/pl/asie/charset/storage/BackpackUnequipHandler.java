package pl.asie.charset.storage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by asie on 1/11/16.
 */
public class BackpackUnequipHandler {
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && event.face == EnumFacing.UP && !event.world.isRemote) {
            ItemStack backpack = event.entityPlayer.getCurrentArmor(2);
            if (backpack != null && backpack.getItem() instanceof ItemBackpack) {
                IBlockState sourceBlock = event.world.getBlockState(event.pos);
                if (sourceBlock.getBlock().isSideSolid(event.world, event.pos, event.face)) {
                    if (backpack.getItem().onItemUse(backpack, event.entityPlayer, event.world, event.pos, event.face, 0, 0, 0)) {
                        event.setCanceled(true);
                        event.entity.setCurrentItemOrArmor(3, null);
                        event.entityPlayer.inventoryContainer.detectAndSendChanges();
                        TileEntity tile = event.world.getTileEntity(event.pos.up());
                        if (tile instanceof TileBackpack) {
                            ((TileBackpack) tile).readFromItemStack(backpack);
                        } else {
                            ModCharsetStorage.logger.error("Something went wrong with placing backpack at " + event.pos.toString() + "! Please report!");
                        }
                    }
                }
            }
        }
    }
}
