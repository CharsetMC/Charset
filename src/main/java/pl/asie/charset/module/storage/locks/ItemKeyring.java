/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.storage.locks;

import baubles.api.BaubleType;
import baubles.api.IBauble;
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
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.storage.IKeyItem;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.inventory.GuiHandlerCharset;
import pl.asie.charset.lib.inventory.ItemHandlerCharset;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemKeyring extends ItemBase implements IKeyItem, IBauble {
    private static class CapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
        private final ItemStack stack;

        private final ItemStackHandler handler = new ItemHandlerCharset(9) {
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
                        if (stackSlot.hasCapability(Capabilities.DYEABLE_ITEM, null)) {
                            IDyeableItem item = stackSlot.getCapability(Capabilities.DYEABLE_ITEM, null);
                            ItemUtils.getTagCompound(stack, true).setInteger("color" + (n++), item.getColor(0));
                        } else {
                            ItemUtils.getTagCompound(stack, true).setInteger("color" + (n++), -1);
                        }
                        m++;
                    }
                }

                stack.setItemDamage(m);
            }
        };

        private final ICapabilityProvider parent;

        public CapabilityProvider(ItemStack stack, ICapabilityProvider parent) {
            this.stack = stack;
            this.parent = parent;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || (parent != null && parent.hasCapability(capability, facing));
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                    ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler)
                    : (parent != null ? parent.getCapability(capability, facing) : null);
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
        setTranslationKey("charset.keyring");
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
        return new CapabilityProvider(stack, super.initCapabilities(stack, nbt));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        if (stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            StringBuilder builder = new StringBuilder();
            int count = 0;
            while (compound.hasKey("color" + count, Constants.NBT.TAG_ANY_NUMERIC)) {
                int color = compound.getInteger("color" + count);
                if (count > 0) {
                    if ((count % 5) == 0) {
                        builder.append(',');
                        tooltip.add(builder.toString());
                        builder = new StringBuilder();
                    } else {
                        builder.append(", ");
                    }
                }

                builder.append(LockEventHandler.getColorDyed(color));
                count++;
            }

            if (count > 0) {
                tooltip.add(builder.toString());
            }
        }

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
                if (((IKeyItem) inStack.getItem()).canUnlock(lock, inStack)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if (tintIndex > 0 && stack.hasTagCompound() && stack.getTagCompound().hasKey("color" + (tintIndex - 1))) {
                return stack.getTagCompound().getInteger("color" + (tintIndex - 1)) | 0xFF000000;
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.TRINKET;
    }
}
