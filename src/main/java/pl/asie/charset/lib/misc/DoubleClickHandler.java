package pl.asie.charset.lib.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pl.asie.charset.lib.CharsetLib;

import java.util.WeakHashMap;

public class DoubleClickHandler {
	private final WeakHashMap<EntityPlayer, Long> lastClickMap = new WeakHashMap<>();

	public DoubleClickHandler() {

	}

	public void markLastClick(EntityPlayer player) {
		lastClickMap.put(player, player.getEntityWorld().getTotalWorldTime());
	}

	public boolean isDoubleClick(EntityPlayer player) {
		Long lastClick = lastClickMap.get(player);
		return lastClick != null && player.getEntityWorld().getTotalWorldTime() - lastClick < CharsetLib.doubleClickDuration;
	}

}
