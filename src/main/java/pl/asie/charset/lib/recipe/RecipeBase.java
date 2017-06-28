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

package pl.asie.charset.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.lib.utils.ThreeState;

public abstract class RecipeBase extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	private final String group;
	private final ThreeState hidden;

	public RecipeBase(JsonContext context, JsonObject object) {
		if (object != null && context != null) {
			if (object.has("group")) {
				group = JsonUtils.getString(object, "group");
			} else {
				group = "";
			}

			if (object.has("hidden")) {
				hidden = JsonUtils.getBoolean(object, "hidden") ? ThreeState.YES : ThreeState.NO;
			} else {
				hidden = ThreeState.MAYBE;
			}
		} else {
			group = "";
			hidden = ThreeState.MAYBE;
		}
	}

	public RecipeBase(String group) {
		this.group = group;
		this.hidden = ThreeState.MAYBE;
	}

	public RecipeBase(String group, boolean hidden) {
		this.group = group;
		this.hidden = hidden ? ThreeState.YES : ThreeState.NO;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public boolean isHidden() {
		if (hidden == ThreeState.MAYBE) {
			return !getRecipeOutput().isEmpty();
		} else {
			return hidden == ThreeState.YES;
		}
	}
}
