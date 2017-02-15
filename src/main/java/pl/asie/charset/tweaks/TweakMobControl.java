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

package pl.asie.charset.tweaks;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.ModCharsetLib;

public class TweakMobControl extends Tweak {
	private Set<Class<? extends Entity>> disabledClasses = new HashSet<Class<? extends Entity>>();
	private Configuration config;

	public TweakMobControl() {
		super("mobs", "mobControl", "Control mob spawning. Upon enabling, refer to 'tweaks-mobcontrol.cfg'.", false);
	}

	private void reloadConfig() {
		config = new Configuration(ModCharsetLib.instance.getConfigFile("tweaks-mobcontrol.cfg"));

		for (String s : EntityList.getEntityNameList()) {
			Class<? extends Entity> entity = EntityList.NAME_TO_CLASS.get(s);
			if (entity != null && EntityLiving.class.isAssignableFrom(entity)) {
				boolean enabled = config.get("allow", s, true, null).getBoolean();
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

		config.save();
	}

	@Override
	public void enable() {
		reloadConfig();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
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
