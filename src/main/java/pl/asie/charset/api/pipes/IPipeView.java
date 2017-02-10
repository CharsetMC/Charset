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

package pl.asie.charset.api.pipes;

import net.minecraft.item.ItemStack;

import java.util.Collection;

/**
 * This interface is used for viewing items travelling through a pipe.
 * Note that the pipe can decide the contents at its own discretion and
 * that it is not necessarily exhaustive - the idea is to let other mods
 * react - and only react - to the act of items travelling inside a pipe.
 */
public interface IPipeView {
	/**
	 * Get a list of visible stacks travelling in a pipe.
	 */
	Collection<ItemStack> getTravellingStacks();
}
