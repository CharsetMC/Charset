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

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.storage.IKeyItem;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemKeyring extends ItemBase implements IKeyItem {
    private static class CapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
        private final ItemStack stack;

        private final ItemStackHandler handler = new ItemStackHandler(9) {
            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return (stack.getItem() instanceof IKeyItem) ? 1 : 0;
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                int m = 0;
                int n = 0;

                for (int i = 0; i < getSlots(); i++) {
                    ItemStack stackSlot = getStackInSlot(i);
                    if (!stackSlot.isEmpty()) {
                        if (stackSlot.getItem() instanceof IDyeableItem) {
                            ItemUtils.getTagCompound(stack, true).setInteger("color" + (n++), ((IDyeableItem) stackSlot.getItem()).getColor(stackSlot));
                        } else {
                            ItemUtils.getTagCompound(stack, true).setInteger("color" + (n++), -1);
                        }
                        m++;
                    }
                }

                stack.setItemDamage(m);
            }
        };

        public CapabilityProvider(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                    ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler)
                    : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("keys", handler.serializeNBT());
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey("keys", Constants.NBT.TAG_COMPOUND)) {
                handler.deserializeNBT(nbt.getCompoundTag("keys"));
            }
        }
    }

    public ItemKeyring() {
        super();
        setMaxStackSize(1);
        setUnlocalizedName("charset.keyring");
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            if (!world.isRemote) {
                player.openGui(ModCharset.instance, GuiHandlerCharset.KEYRING, player.getEntityWorld(),
                        player.inventory.currentItem, 0, 0);
            }

            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new CapabilityProvider(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        if (ItemKey.DEBUG_KEY_ID) {
            IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack inStack = handler.getStackInSlot(i);
                if (!inStack.isEmpty()) {
                    if (inStack.getItem() == CharsetStorageLocks.keyItem) {
                        tooltip.add(CharsetStorageLocks.keyItem.getKey(inStack));
                    } else {
                        tooltip.add("???");
                    }
                } else {
                    tooltip.add("-");
                }
            }
        }
    }

    @Override
    public boolean canUnlock(String lock, ItemStack stack) {
        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inStack = handler.getStackInSlot(i);
            if (!inStack.isEmpty() && inStack.getItem() instanceof IKeyItem) {
                if (((IKeyItem) inStack.getItem()).canUnlock(lock, stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (tintIndex > 0 && stack.hasTagCompound() && stack.getTagCompound().hasKey("color" + (tintIndex - 1))) {
                int c = stack.getTagCompound().getInteger("color" + (tintIndex - 1));
                if (c >= 0) {
                    return c;
                }
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }
}
