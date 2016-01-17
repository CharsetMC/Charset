package pl.asie.charset.audio;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import pl.asie.charset.audio.tape.ContainerTapeDrive;
import pl.asie.charset.audio.tape.GuiTapeDrive;

public class GuiHandlerAudio implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if (tile instanceof IInteractionObject) {
			Container container = ((IInteractionObject) tile).createContainer(player.inventory, player);
			switch (id) {
				case 1:
					return container instanceof ContainerTapeDrive ? container : null;
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		Container container = (Container) getServerGuiElement(id, player, world, x, y, z);

		switch (id) {
			case 1:
				return new GuiTapeDrive(container);
		}
		return null;
	}
}
