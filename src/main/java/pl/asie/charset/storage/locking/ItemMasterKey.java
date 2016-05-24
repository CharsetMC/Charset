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

package pl.asie.charset.storage.locking;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pl.asie.charset.api.storage.IKeyItem;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemMasterKey extends Item implements IKeyItem {
    public ItemMasterKey() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.masterKey");
    }

    @Override
    public boolean canUnlock(String lock, ItemStack stack) {
        return true;
    }
}
