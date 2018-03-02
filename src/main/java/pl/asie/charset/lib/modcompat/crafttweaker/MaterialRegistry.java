/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.lib.modcompat.crafttweaker;

import com.google.common.base.Joiner;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import jdk.nashorn.internal.scripts.JO;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ThreeState;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.charset.MaterialRegistry")
@ModOnly("charset")
public class MaterialRegistry {
	private static final Joiner JOINER = Joiner.on(',');

	@ZenMethod
	public static boolean registerTypes(IItemStack stack, String... tags) {
		ItemStack mcStack = CraftTweakerMC.getItemStack(stack);
		if (mcStack.isEmpty()) {
			return false;
		}
		CraftTweakerAPI.apply(new IAction() {
			@Override
			public void apply() {
				ItemMaterial material = ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(mcStack);
				ItemMaterialRegistry.INSTANCE.registerTypes(material, tags);
			}

			@Override
			public String describe() {
				return "Registering stack " + stack + " as material with types " + JOINER.join(tags);
			}
		});
		return true;
	}

	@ZenMethod
	public static boolean registerRelation(IItemStack from, String what, IItemStack to) {
		ItemStack mcStackFrom = CraftTweakerMC.getItemStack(from);
		ItemStack mcStackTo = CraftTweakerMC.getItemStack(to);
		if (mcStackFrom.isEmpty() || mcStackTo.isEmpty()) {
			return false;
		}
		CraftTweakerAPI.apply(new IAction() {
			@Override
			public void apply() {
				ItemMaterialRegistry.INSTANCE.registerRelation(ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(mcStackFrom), ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(mcStackTo), what);
			}

			@Override
			public String describe() {
				return "Registering material relation (" + from + " --[" + what + "]-> " + to + ")";
			}
		});
		return true;
	}
}
