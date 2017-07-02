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

package pl.asie.charset.module.tweaks.tnt;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "tweak.pushableTnt",
		description = "Allows players to push TNT around by hand or projectile",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakPushableTNT {
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(EntityTNTImproved.class, "tnt", 64, 1, true);
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
