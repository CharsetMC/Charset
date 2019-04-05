/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.utils.ThreeState;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.charset.Registry")
@ModOnly("charset")
public class Registry {
	@ZenMethod
	public static boolean allow(String key, IItemStack stack) {
		return allow(key, CraftTweakerMC.getItemStack(stack).getItem().getRegistryName().toString());
	}

	@ZenMethod
	public static boolean forbid(String key, IItemStack stack) {
		return forbid(key, CraftTweakerMC.getItemStack(stack).getItem().getRegistryName().toString());
	}

	@ZenMethod
	public static boolean allow(String key, String location) {
		if (!location.contains(":")) {
			return false;
		}
		CraftTweakerAPI.apply(new IMCAction(key, new ResourceLocation(location), "Allowing") {
			@Override
			public void apply() {
				CharsetIMC.INSTANCE.add(ThreeState.YES, this.key, this.location);
			}
		});
		return true;
	}

	@ZenMethod
	public static boolean forbid(String key, String location) {
		if (!location.contains(":")) {
			return false;
		}
		CraftTweakerAPI.apply(new IMCAction(key, new ResourceLocation(location), "Forbidding") {
			@Override
			public void apply() {
				CharsetIMC.INSTANCE.add(ThreeState.NO, this.key, this.location);
			}
		});
		return true;
	}

	public static abstract class IMCAction implements IAction {
		protected final String key;
		protected final ResourceLocation location;
		private final String descriptor;

		public IMCAction(String key, ResourceLocation location, String descriptor) {
			this.key = key;
			this.location = location;
			this.descriptor = descriptor;
		}

		@Override
		public String describe() {
			return descriptor + " Charset functionality " + key + " for block " + location;
		}
	}
}
