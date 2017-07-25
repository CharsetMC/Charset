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
 *
 * Copyright (c) 2014 copygirl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.module.tweaks.dynlights;

import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.Light;
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
	public void onPreInit(FMLPreInitializationEvent event) {
		enableExplosionLights = config.getBoolean("explosion", "sources", true, "Light sources based on being ready for an explosion.");
		enableFireLights = config.getBoolean("fire", "sources", true, "Light sources based on being lit on fire.");
		enableItemLights = config.getBoolean("items", "sources", true, "Light sources based on items.");
		enableEntityLights = config.getBoolean("entities", "holders", true, "Light sources held by non-player entities.");
		enablePlayerLights = config.getBoolean("players", "holders", true, "Light sources held by players.");
		blockBrightnessDivider = 1f / config.getFloat("blockBrightnessMultiplier", "config", 0.4f, 0, 1, "The multiplier for block-derived light brightness.");
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
