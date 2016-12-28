package pl.asie.charset.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * Created by asie on 12/27/16.
 */
public class GuiHandlerCrafting implements IGuiHandler {
	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 1) {
			return new ContainerPocket(player);
		}

		return null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == 1) {
			return new GuiPocketTable(new ContainerPocket(player));
		}

		return null;
	}
}
