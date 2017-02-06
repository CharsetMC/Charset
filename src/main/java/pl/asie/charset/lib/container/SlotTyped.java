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

package pl.asie.charset.lib.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class SlotTyped extends SlotItemHandler {
	private Predicate<ItemStack>[] allowedTypes;
	
	public SlotTyped(IItemHandler handler, int par2, int par3, int par4, Predicate<ItemStack>... allowedTypes) {
		super(handler, par2, par3, par4);
		this.allowedTypes = allowedTypes;
	}

	@Override
    public boolean isItemValid(ItemStack stack) {
		for (Predicate<ItemStack> o: allowedTypes) {
        	if (o.test(stack)) {
		        return true;
	        }
        }
        return false;
    }
}
