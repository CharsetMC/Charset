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

package pl.asie.charset.module.tweaks.dynlights;

import com.elytradev.mirage.event.GatherLightsEvent;
import com.elytradev.mirage.lighting.Light;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CharsetModule(
		name = "tweak.dynamicLights",
		description = "Dynamic lights, based on Albedo!",
		dependencies = {"mod:albedo"},
		isClientOnly = true,
		profile = ModuleProfile.TESTING
)
public class CharsetTweakDynamicLights {
	public static class LightKey {
		public final Item item;
		public final int meta;

		public LightKey(Item item, int meta) {
			this.item = item;
			this.meta = meta;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof LightKey)) return false;
			LightKey other = (LightKey) o;
			return other.item == item && other.meta == meta;
		}

		@Override
		public int hashCode() {
			return 31 * item.hashCode() + meta;
		}
	}

	@CharsetModule.Configuration
	public static Configuration config;
	private static Map<LightKey, float[]> lightData = new HashMap<>();

	public static boolean enablePlayerLights, enableEntityLights, enableFireLights, enableExplosionLights, enableItemLights;
	public static float blockBrightnessDivider;

	@Mod.EventHandler
	public void loadConfig(CharsetLoadConfigEvent event) {
		enableExplosionLights = ConfigUtils.getBoolean(config, "sources", "explosion",  true, "Light sources based on being ready for an explosion.", false);
		enableFireLights = ConfigUtils.getBoolean(config, "sources", "fire",  true, "Light sources based on being lit on fire.", false);
		enableItemLights = ConfigUtils.getBoolean(config, "sources", "items", true, "Light sources based on items.", false);
		enableEntityLights = ConfigUtils.getBoolean(config, "holders", "entities", true, "Light sources held by non-player entities.", false);
		enablePlayerLights = ConfigUtils.getBoolean(config, "holders", "players", true, "Light sources held by players.", false);
		blockBrightnessDivider = 1f / ConfigUtils.getFloat(config, "general", "blockBrightnessMultiplier", 0.4f, 0, 1, "The multiplier for block-derived light brightness.", false);
	}

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
	}

	private Light getLight(double x, double y, double z, ItemStack s) {
		LightKey key = new LightKey(s.getItem(), s.getMetadata());
		float[] data = lightData.computeIfAbsent(key, (lightKey -> {
			try {
				if (key.item instanceof ItemBlock) {
					IBlockState b = ItemUtils.getBlockState(s);
					if (b.getLightValue() > 0) {
						return new float[] {1, 1, 1, b.getLightValue() / (16.0f * blockBrightnessDivider), b.getLightValue() / blockBrightnessDivider};
					}
				}

				return null;
			} catch (Exception e) {
				return null;
			}
		}));

		if (data != null) {
			return Light.builder().pos(x, y, z).color(data[0], data[1], data[2], data[3]).radius(data[4]).build();
		} else {
			return null;
		}
	}

	private void addLight(List<Light> lights, Entity e) {
		if (enableExplosionLights) {
			if (e instanceof EntityCreeper) {
				float brightness = ((EntityCreeper) e).getCreeperFlashIntensity(Minecraft.getMinecraft().getRenderPartialTicks());
				if (brightness > 0.001f) {
					lights.add(Light.builder().pos(e).color(1, 1, 1, brightness).radius(10 / blockBrightnessDivider).build());
				}
			} else if (e instanceof EntityTNTPrimed) {
				lights.add(Light.builder().pos(e).color(1, 1, 1, 0.5f).radius(10 / blockBrightnessDivider).build());
			}
		}

		if (enableFireLights && e.isBurning()) {
			float lv = Blocks.FIRE.getDefaultState().getLightValue();
			lights.add(Light.builder().pos(e).color(1, 1, 1, lv / (16.0f * blockBrightnessDivider)).radius(lv / blockBrightnessDivider).build());
		}

		for (ItemStack s : e.getHeldEquipment()) {
			Light light = getLight(e.posX, e.posY, e.posZ, s);
			if (light != null) {
				lights.add(light);
			}
		}
	}

	@SubscribeEvent
	public void onGatherLights(GatherLightsEvent event) {
		List<Light> lights = event.getLightList();
		EntityPlayerSP player = Minecraft.getMinecraft().player;

		if (enableEntityLights) {
			player.getEntityWorld().loadedEntityList.forEach((e) -> {
				if (!enablePlayerLights && e instanceof EntityPlayer) return;
				addLight(lights, e);
			});
		} else if (enablePlayerLights) {
			player.getEntityWorld().playerEntities.forEach((e) -> addLight(lights, e));
		}
	}
}
