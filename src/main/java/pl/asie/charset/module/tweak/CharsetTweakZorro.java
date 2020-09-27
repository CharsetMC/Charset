/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.Set;

@CharsetModule(
		name = "tweak.zorro",
		profile = ModuleProfile.TESTING
)
public class CharsetTweakZorro {
	@SubscribeEvent
	public void doTheZorroThing(PlayerInteractEvent.EntityInteract event) {
		EntityPlayer player = event.getEntityPlayer();
		if (player.world.isRemote) return;
		if (player.isRiding()) return;
		if (!(event.getTarget() instanceof EntityHorse)) return;
		EntityHorse horse = (EntityHorse) event.getTarget();
		if (player.fallDistance <= 2) return;
		if (!horse.isHorseSaddled()) return;
		if (horse.getLeashed()) {
			if (!(horse.getLeashHolder() instanceof EntityLeashKnot)) return;
			horse.getLeashHolder().processInitialInteract(player, EnumHand.MAIN_HAND);
		}
		boolean awesome = false;
		ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (player.fallDistance > 5 && !heldStack.isEmpty()) {
			Item held = heldStack.getItem();
			boolean has_baby = false;
			if (player.getRidingEntity() instanceof EntityAgeable) {
				EntityAgeable ea = (EntityAgeable) player.getRidingEntity();
				has_baby = ea.isChild();
			}
			awesome = held instanceof ItemSword || held instanceof ItemBow || player.getRidingEntity() instanceof EntityPlayer || has_baby;
			if (!awesome) {
				Set<String> classes = held.getToolClasses(heldStack);
				awesome |= classes.contains("axe");
			}
		}
		if (awesome) {
			horse.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("speed"), 20 * 40, 2, false, false));
			horse.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("resistance"), 20 * 40, 1, true, true));
			horse.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("jump_boost"), 20 * 40, 1, true, true));
		} else {
			horse.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("speed"), 20 * 8, 1, false, false));
		}
		horse.playLivingSound();
	}
}
