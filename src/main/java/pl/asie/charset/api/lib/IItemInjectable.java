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

package pl.asie.charset.api.lib;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

@Deprecated
public interface IItemInjectable {
	/**
	 * Checks if items can be injected from a given side.
	 *
	 * @param side
	 * @return True if items can be injected from this side.
	 */
	boolean canInjectItems(EnumFacing side);

	/**
	 * Tries to inject an item into the pipe.
	 *
	 * @param stack    The stack offered for injection. Make a copy before editing.
	 * @param side     The side the item is being injected from.
	 * @param simulate If true, no actual injection should take place.
	 * @return The amount of items used from the stack.
	 */
	int injectItem(ItemStack stack, EnumFacing side, boolean simulate);
}
