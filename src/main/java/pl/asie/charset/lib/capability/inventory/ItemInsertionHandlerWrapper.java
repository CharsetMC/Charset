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

package pl.asie.charset.lib.capability.inventory;

import mcmultipart.capabilities.ICapabilityWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.Capabilities;

import java.util.Collection;

public class ItemInsertionHandlerWrapper implements ICapabilityWrapper<IItemInsertionHandler> {
    private class WrappedInserter implements IItemInsertionHandler {
        private final Collection<IItemInsertionHandler> receivers;

        WrappedInserter(Collection<IItemInsertionHandler> receivers) {
            this.receivers = receivers;
        }

        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            ItemStack toInsert = stack;
            for (IItemInsertionHandler insertionHandler : receivers) {
                if (toInsert.isEmpty())
                    break;
                toInsert = insertionHandler.insertItem(toInsert, simulate);
            }
            return toInsert;
        }
    }

    @Override
    public Capability<IItemInsertionHandler> getCapability() {
        return Capabilities.ITEM_INSERTION_HANDLER;
    }

    @Override
    public IItemInsertionHandler wrapImplementations(Collection<IItemInsertionHandler> implementations) {
        return new WrappedInserter(implementations);
    }
}
