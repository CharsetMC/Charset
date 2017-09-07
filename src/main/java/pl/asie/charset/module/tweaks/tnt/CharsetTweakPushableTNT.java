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
