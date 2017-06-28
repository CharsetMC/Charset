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

package pl.asie.charset.module.storage.locks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.storage.IKeyItem;

import java.util.List;

public class ItemKeyring extends ItemLockingDyeable implements IKeyItem {
    public ItemKeyring() {
        super();
        setUnlocalizedName("charset.keyring");
    }

    // ""security""
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("keys")) {
            NBTTagCompound tag = stack.getTagCompound().copy();
            tag.removeTag("keys");
            return tag;
        } else {
            return stack.getTagCompound();
        }
    }

    public int getKeyCount(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("keys", Constants.NBT.TAG_LIST) ? stack.getTagCompound().getTagList("keys", Constants.NBT.TAG_STRING).tagCount() : 0;
    }

    public String getKey(ItemStack stack, int i) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("keys", Constants.NBT.TAG_LIST) ? stack.getTagCompound().getTagList("keys", Constants.NBT.TAG_STRING).getStringTagAt(i) : "null";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (ItemKey.DEBUG_KEY_ID) {
            for (int i = 0; i < getKeyCount(stack); i++) {
                tooltip.add(getKey(stack, i));
            }
        }
    }

    @Override
    public boolean canUnlock(String lock, ItemStack stack) {
        for (int i = 0; i < getKeyCount(stack); i++) {
            if (getKey(stack, i).equals(lock)) {
                return true;
            }
        }
        return false;
    }
}
