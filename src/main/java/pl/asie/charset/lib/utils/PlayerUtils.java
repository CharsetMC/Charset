/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
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
				if (Minecraft.getMinecraft().world != null) {
					return Minecraft.getMinecraft().world.getPlayerEntityByName(name);
				}
				return null;
			}
		}

		for (EntityPlayerMP target : server.getPlayerList().getPlayers()) {
			if (target.getName().equals(name)) {
				return target;
			}
		}
		return null;
	}

	public static boolean isCreative(EntityPlayer player) {
		return player.capabilities.isCreativeMode;
	}

	// Weird Factorization stuff
	public static int getPuntStrengthOrWeakness(EntityPlayer player) {
		if (player == null) return 1;
		//strength * knocback
		int strength = 0;
		PotionEffect p_str = player.getActivePotionEffect(MobEffects.STRENGTH);
		PotionEffect p_wea = player.getActivePotionEffect(MobEffects.WEAKNESS);
		if (p_str != null) {
			strength += p_str.getAmplifier() + 1;
		}
		if (p_wea != null) {
			strength -= p_wea.getAmplifier() + 1;
		}
		int knockback = EnchantmentHelper.getKnockbackModifier(player);
		return strength * knockback;
	}

	public static int getPuntStrengthInt(EntityPlayer player) {
		int str = getPuntStrengthOrWeakness(player);
		return Math.min(1, str);
	}

	public static double getPuntStrengthMultiplier(EntityPlayer player) {
		int str = getPuntStrengthOrWeakness(player);
		if (str == 0) return 1;
		if (str < 1) return 1.0 / -str;
		return str;
	}
}
