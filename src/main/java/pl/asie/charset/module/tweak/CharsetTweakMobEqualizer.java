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

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

@CharsetModule(
		name = "tweak.equalizeMobChances",
		description = "Allows a mob wearing armor comparable to yours to rarely spawn",
		profile = ModuleProfile.TESTING
)
public class CharsetTweakMobEqualizer {
	@SubscribeEvent(priority = EventPriority.LOW)
	public void upgradeMob(LivingSpawnEvent.SpecialSpawn event) {
		EnumDifficulty difficulty = event.getWorld().getDifficulty();
		if (difficulty == null || difficulty.getDifficultyId() <= 1) {
			return;
		}
		if (!(event.getEntityLiving() instanceof EntityMob)) {
			return;
		}
		EntityMob ent = (EntityMob) event.getEntityLiving();
		// TODO: Evaluate this system.
		// 1) Is canPickUpLoot() a valid check?
		// 2) Should we add more granular setups (like only some elements of armor, but at a higher frequency)?
		if (event.getWorld().rand.nextInt(400) > difficulty.getDifficultyId()) {
			return;
		}
		if (!ent.canPickUpLoot()) return;
		EntityPlayer template = pickNearPlayer(event);
		if (template == null) {
			return;
		}
		int equipmentCount = 0;
		ItemStack[] equipmentCopies = new ItemStack[6];
		boolean copyArmor = event.getEntity() instanceof IRangedAttackMob || event.getWorld().rand.nextBoolean();
		boolean copyWeapon = !(event.getEntity() instanceof IRangedAttackMob) || event.getWorld().rand.nextBoolean();

		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && copyArmor) {
				ItemStack is = template.getItemStackFromSlot(slot);
				if (!is.isEmpty() && is.getItem().isValidArmor(is, slot, ent)) {
					equipmentCopies[slot.ordinal()] = is.copy();
					equipmentCount++;
				} else {
					equipmentCopies[slot.ordinal()] = ItemStack.EMPTY;
				}
			}
		}

		List<ItemStack> carriedWeapons = new ArrayList<ItemStack>();
		if (copyWeapon) {
			ItemStack currentWeapon = ent.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
			double currentWeaponDmg = ItemUtils.getAttributeValue(EntityEquipmentSlot.MAINHAND, currentWeapon, SharedMonsterAttributes.ATTACK_DAMAGE);
			for (int i = 0; i < 9; i++) {
				ItemStack playerWeapon = template.inventory.getStackInSlot(i);
				if (playerWeapon.isEmpty() || playerWeapon.getCount() != 1 || playerWeapon.getMaxStackSize() != 1) {
					continue;
				}
				EnumAction act = playerWeapon.getItemUseAction();
				if (act != EnumAction.BLOCK && act != EnumAction.NONE && act != EnumAction.BOW) {
					continue;
				}
				double playerWeaponDmg = ItemUtils.getAttributeValue(EntityEquipmentSlot.MAINHAND, playerWeapon, SharedMonsterAttributes.ATTACK_DAMAGE);
				if (playerWeaponDmg > currentWeaponDmg) {
					carriedWeapons.add(playerWeapon.copy());
				}
			}
		}

		if (!carriedWeapons.isEmpty()) {
			equipmentCopies[0] = carriedWeapons.get(event.getWorld().rand.nextInt(carriedWeapons.size())).copy();
			equipmentCount++;
		}

		if (equipmentCount <= 0) {
			return;
		}

		event.setCanceled(true);
		ent.onInitialSpawn(ent.world.getDifficultyForLocation(new BlockPos(event.getEntity())), null);
		// We need to cancel the event so that we can call this before the below happens
		ent.setCanPickUpLoot(false);

		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (equipmentCopies[slot.ordinal()] != null) {
				ent.setItemStackToSlot(slot, equipmentCopies[slot.ordinal()]);
			}
			ent.setDropChance(slot, 0);
		}
	}

	private EntityPlayer pickNearPlayer(LivingSpawnEvent.SpecialSpawn event) {
		//See "Algorithm R (Reservoir sampling)" in "The Art of Computer Programming: Seminumerical Algorithms" by Donald Knuth, Chapter 3.4.2, page 144.
		double maxDistanceSq = Math.pow(16*8, 2);
		EntityPlayer secretary = null;
		int interviews = 0;
		for (EntityPlayer player : event.getWorld().playerEntities) {
			if (player.capabilities.isCreativeMode) {
				continue;
			}
			if (event.getEntity().getDistanceSq(player) > maxDistanceSq) {
				continue;
			}
			interviews++;
			int M = event.getWorld().rand.nextInt(interviews) + 1 /* converts from [0,i-1] to [1, i] */;
			if (M <= 1 /* we need only 1 sample */) {
				secretary = player;
			}
		}
		return secretary;
	}
}
