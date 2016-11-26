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

package pl.asie.charset.tweaks.tnt;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import pl.asie.charset.storage.barrel.EntityMinecartDayBarrel;
import pl.asie.charset.tweaks.ModCharsetTweaks;
import pl.asie.charset.tweaks.Tweak;

public class TweakImprovedTNT extends Tweak {
	public TweakImprovedTNT() {
		super("tweaks", "improvedTNT", "Makes primed TNT hittable.", true);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public void enable() {
		EntityRegistry.registerModEntity(new ResourceLocation("charsettweaks:tnt"), EntityTNTImproved.class, "charsettweaks:tnt", 1, ModCharsetTweaks.instance, 64, 1, true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getEntity().getClass() == EntityTNTPrimed.class) {
			event.setCanceled(true);
			EntityTNTImproved tnt = new EntityTNTImproved(event.getWorld(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ,
					((EntityTNTPrimed) event.getEntity()).getTntPlacedBy());
			tnt.setFuse(((EntityTNTPrimed) event.getEntity()).getFuse());
			event.getWorld().spawnEntity(tnt);
		}
	}
}
