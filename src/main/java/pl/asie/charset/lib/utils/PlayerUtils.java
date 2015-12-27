package pl.asie.charset.lib.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;

public final class PlayerUtils {
	private PlayerUtils() {

	}

	public static boolean isFakePlayer(EntityPlayer player) {
		return player instanceof FakePlayer || !player.addedToChunk;
	}
}
