package pl.asie.charset.lib.container;

import net.minecraft.entity.player.EntityPlayer;

public interface IContainerHandler {
	void onOpenedBy(EntityPlayer player);
	void onClosedBy(EntityPlayer player);
	boolean isUsableByPlayer(EntityPlayer player);
}
