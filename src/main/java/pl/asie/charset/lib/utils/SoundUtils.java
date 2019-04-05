/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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
