package pl.asie.charset.lib.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

public final class SoundUtils {
	private SoundUtils() {

	}

	public static void playSoundRemote(EntityPlayer player, Vec3d pos, double radius, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		if (player instanceof EntityPlayerMP && !player.getEntityWorld().isRemote) {
			if (player.getDistanceSq(pos.x, pos.y, pos.z) <= radius * radius) {
				SPacketSoundEffect soundEffect = new SPacketSoundEffect(
						soundIn, category,
						pos.x, pos.y, pos.z,
						volume, pitch
				);
				((EntityPlayerMP) player).connection.sendPacket(soundEffect);
			}
		}
	}
}
