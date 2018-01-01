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

package pl.asie.charset.module.tweak;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.HashSet;
import java.util.Set;

@CharsetModule(
		name = "tweak.mobControl",
		description = "Allow type-based control of mob spawning.",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakMobControl {
	@CharsetModule.Configuration
	public static Configuration config;

	private Set<Class<? extends Entity>> disabledClasses = new HashSet<Class<? extends Entity>>();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		for (ResourceLocation s : EntityList.getEntityNameList()) {
			Class<? extends Entity> entity = EntityList.getClass(s);
			if (entity != null && EntityLiving.class.isAssignableFrom(entity)) {
				boolean enabled = config.get("allow", s.toString(), true, null).getBoolean();
				if (!enabled) {
					disabledClasses.add(entity);
				}
			}
		}

		Set<String> enderCarrySet = new HashSet<>();
		for (Block b : Block.REGISTRY) {
			if (EntityEnderman.getCarriable(b)) {
				enderCarrySet.add(b.getRegistryName().toString());
				EntityEnderman.setCarriable(b, false);
			}
		}
		String[] enderCarry = enderCarrySet.toArray(new String[enderCarrySet.size()]);

		if (!config.hasCategory("tweaks")) {
			enderCarry = config.getStringList("endermanCarriable", "tweaks", enderCarry, "The list of blocks carriable by endermen.");
		}

		for (String s : enderCarry) {
			Block b = Block.getBlockFromName(s);
			if (b != null && b != Blocks.AIR) {
				EntityEnderman.setCarriable(b, true);
			}
		}
	}

	@SubscribeEvent
	public void checkSpawn(LivingSpawnEvent.CheckSpawn event) {
		if (disabledClasses.contains(event.getEntity().getClass())) {
			event.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public void checkJoinWorld(EntityJoinWorldEvent event) {
		if (disabledClasses.contains(event.getEntity().getClass())) {
			event.setCanceled(true);
		}
	}
}
