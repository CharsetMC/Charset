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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;

import java.util.Set;

@CharsetModule(
		name = "tweak.zorro"
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
			if (!(horse.getLeashedToEntity() instanceof EntityLeashKnot)) return;
			horse.getLeashedToEntity().processInitialInteract(player, EnumHand.MAIN_HAND);
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
