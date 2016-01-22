package pl.asie.charset.audio;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import pl.asie.charset.audio.tape.ContainerTapeDrive;
import pl.asie.charset.audio.tape.GuiTapeDrive;
import pl.asie.charset.audio.tape.PartTapeDrive;

public class GuiHandlerAudio implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, new BlockPos(x, y, z));
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(PartSlot.VALUES[id]);
			if (part instanceof PartTapeDrive) {
				return new ContainerTapeDrive(((PartTapeDrive) part).inventory, player.inventory);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, new BlockPos(x, y, z));
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(PartSlot.VALUES[id]);
			if (part instanceof PartTapeDrive) {
				return new GuiTapeDrive(new ContainerTapeDrive(((PartTapeDrive) part).inventory, player.inventory), (PartTapeDrive) part);
			}
		}
		return null;
	}
}
