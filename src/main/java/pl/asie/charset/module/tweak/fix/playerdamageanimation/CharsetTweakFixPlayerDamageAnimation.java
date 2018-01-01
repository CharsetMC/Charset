/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tweak.fix.playerdamageanimation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;

import java.util.ArrayDeque;
import java.util.Queue;

@CharsetModule(
		name = "tweak.fix.playerdamageanimation",
		description = "Fixes player directional damage animation.",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakFixPlayerDamageAnimation {
	@CharsetModule.PacketRegistry("fixPlyrDmgAnim")
	private PacketRegistry registry;
	private final Queue<EntityLivingBase> players = new ArrayDeque<>();

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		registry.registerPacket(0x01, PacketSyncAttackValue.class);
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayerMP && !event.getEntityLiving().getEntityWorld().isRemote) {
			players.add(event.getEntityLiving());
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER) {
			EntityLivingBase player;
			while (players.size() > 0) {
				player = players.remove();
				registry.sendTo(new PacketSyncAttackValue(player), (EntityPlayer) player);
			}
		}
	}
}
