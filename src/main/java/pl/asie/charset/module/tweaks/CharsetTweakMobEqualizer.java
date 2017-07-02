/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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

package pl.asie.charset.module.tweaks;

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
			if (event.getEntity().getDistanceSqToEntity(player) > maxDistanceSq) {
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
