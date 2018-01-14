/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.lib.EntityGatherItemsEvent;
import pl.asie.charset.api.lib.IMultiblockStructure;
import pl.asie.charset.api.locks.Lockable;
import pl.asie.charset.api.storage.IKeyItem;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.item.FontRendererFancy;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.ThreeState;
import pl.asie.charset.lib.utils.color.Colorspace;
import pl.asie.charset.lib.utils.color.Colorspaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class LockEventHandler {
    public static CapabilityProviderFactory<Lockable> PROVIDER;

    public static Lockable getLock(TileEntity tile) {
        if (tile != null) {
            if (tile.hasCapability(Capabilities.MULTIBLOCK_STRUCTURE, null)) {
                IMultiblockStructure structure = tile.getCapability(Capabilities.MULTIBLOCK_STRUCTURE, null);
                Iterator<BlockPos> iterator = structure.iterator();
                while (iterator.hasNext()) {
                    TileEntity tile2 = tile.getWorld().getTileEntity(iterator.next());
                    if (tile2 != null && tile2.hasCapability(Capabilities.LOCKABLE, null)) {
                        Lockable lock = tile2.getCapability(Capabilities.LOCKABLE, null);
                        if (lock.hasLock() && lock.getLock().isLockValid(tile2) && lock.getLock().isLocked()) {
                            return lock;
                        }
                    }
                }
            } else if (tile.hasCapability(Capabilities.LOCKABLE, null)) {
                Lockable lock = tile.getCapability(Capabilities.LOCKABLE, null);
                if (lock.hasLock() && lock.getLock().isLockValid(tile) && lock.getLock().isLocked()) {
                    return lock;
                }
            }
        }

        return null;
    }

    public static Collection<ItemStack> getPotentialKeys(Entity player) {
        Collection<ItemStack> stacks = new ArrayList<>();
        MinecraftForge.EVENT_BUS.post(new EntityGatherItemsEvent(player, stacks, true, true));
        return stacks;
    }


    public static boolean unlockOrRaiseError(EntityPlayer player, TileEntity tile, Lockable lock) {
        if (player.getEntityWorld().isRemote) {
            return true;
        }

        boolean canUnlock = false;
        for (ItemStack stack : getPotentialKeys(player)) {
            if (!stack.isEmpty() && stack.getItem() instanceof IKeyItem) {
                IKeyItem key = (IKeyItem) stack.getItem();
                canUnlock = key.canUnlock(lock.getLock().getLockKey(), stack);
                if (canUnlock) {
                    break;
                }
            }
        }

        if (!canUnlock) {
            ITextComponent displayName = tile.getDisplayName();
            if (displayName == null) {
                displayName = new TextComponentTranslation(tile.getBlockType().getUnlocalizedName() + ".name");
            }
            new Notice(tile, new TextComponentTranslation("container.isLocked", displayName)).sendTo(player);
            player.getEntityWorld().playSound(player, tile.getPos(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        return canUnlock;
    }

    private static final ResourceLocation LOCKABLE = new ResourceLocation("charset:lockable");

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<TileEntity> event) {
        TileEntity tile = event.getObject();
        ResourceLocation location = TileEntity.getKey(tile.getClass());
        if (location == null) // f.e. IC2 energy net internals
            return;

        ThreeState state = CharsetIMC.INSTANCE.allows("lock", location);
        boolean hasCap = state == ThreeState.YES;

        if (state == ThreeState.MAYBE) {
            if (tile instanceof TileEntityChest) {
                hasCap = true;
            }

            if ("minecraft".equals(location.getResourceDomain()) && tile instanceof TileEntityLockable) {
                hasCap = true;
            }
        }

        if (hasCap) {
            if (PROVIDER == null) {
                PROVIDER = new CapabilityProviderFactory<>(Capabilities.LOCKABLE, Capabilities.LOCKABLE_STORAGE);
            }
            event.addCapability(LOCKABLE, PROVIDER.create(new Lockable(tile)));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        Lockable lockable = getLock(tile);
        if (lockable != null && !unlockOrRaiseError(event.getEntityPlayer(), tile, lockable)) {
            event.setUseBlock(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        Lockable lockable = getLock(tile);
        if (lockable != null && !unlockOrRaiseError(event.getEntityPlayer(), tile, lockable)) {
            event.setUseBlock(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        IBlockState state = event.getState();
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tile = event.getWorld().getTileEntity(event.getPos());
            Lockable lockable = getLock(tile);
            if (lockable != null && !unlockOrRaiseError(event.getPlayer(), tile, lockable)) {
                event.setCanceled(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static String getColorDyed(int color) {
        color &= 0xFFFFFF;

        double maxDistance = Double.MAX_VALUE;
        EnumDyeColor closestColor = null;

        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            int c = ColorUtils.toIntColor(dyeColor) & 0xFFFFFF;
            if (color == c) {
                closestColor = dyeColor;
                break;
            }

            double d = Colorspaces.getColorDistance(c, color, Colorspace.LAB);
            if (d < maxDistance) {
                maxDistance = d;
                closestColor = dyeColor;
            }
        }

        return FontRendererFancy.getColorFormat(color)
            + I18n.format(ColorUtils.getLangEntry("charset.color.", closestColor))
            + FontRendererFancy.getColorResetFormat();
    }
}
