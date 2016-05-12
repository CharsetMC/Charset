package pl.asie.charset.lib.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerUtils {
	public static boolean isFakePlayer(EntityPlayer player) {
		return player instanceof FakePlayer || !player.addedToChunk;
	}

	public static EntityPlayer find(MinecraftServer server, String name) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			if (server == null) {
				if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().theWorld != null) {
					return Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);
				}
				return null;
			}
		}

		for (EntityPlayerMP target : server.getPlayerList().getPlayerList()) {
			if (target.getName().equals(name)) {
				return target;
			}
		}
		return null;
	}
}
