package pl.asie.charset.storage.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

public class StorageGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile instanceof IInteractionObject) {
            Container container = ((IInteractionObject) tile).createContainer(player.inventory, player);
            switch (id) {
                case 1:
                    return container instanceof ContainerBackpack ? container : null;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Container container = (Container) getServerGuiElement(id, player, world, x, y, z);

        switch (id) {
            case 1:
                return new GuiBackpack(container);
        }
        return null;
    }
}
