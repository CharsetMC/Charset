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

package pl.asie.charset.module.storage.barrels;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.api.lib.ICacheable;
import pl.asie.charset.api.locks.Lockable;
import pl.asie.charset.api.storage.IBarrel;
import pl.asie.charset.lib.block.ITileWrenchRotatable;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.CapabilityCache;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.lib.utils.*;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityDayBarrel extends TileBase implements IBarrel, ICacheable, ITickable, IAxisRotatable, ITileWrenchRotatable {
    public ItemStack item = ItemStack.EMPTY;
    public ItemMaterial woodLog, woodSlab;
    public Orientation orientation = Orientation.FACE_UP_POINT_NORTH;
    public Set<BarrelUpgrade> upgrades = EnumSet.noneOf(BarrelUpgrade.class);
    Object notice_target = this;

    protected final Lockable lockable = new Lockable(this);
    protected final InsertionHandler insertionView = new InsertionHandler();
    protected final ExtractionHandler extractionView = new ExtractionHandler();
    protected final ReadableItemHandler readOnlyView = new ReadableItemHandler();
    protected boolean isEntity;

    private CapabilityCache.Single<IItemHandler> helperTop, helperBottom;
    private ProxiedBlockAccess woodLogAccess;

    @Override
    public boolean isCacheValid() {
        return !isEntity && !isInvalid();
    }

    public abstract class BaseItemHandler implements ICacheable, IItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = item;
            if (!stack.isEmpty() && stack.getCount() > item.getMaxStackSize()) {
                stack = stack.copy();
                stack.setCount(item.getMaxStackSize());
            }
            return stack;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return !item.isEmpty() ? item.getMaxStackSize() : 64;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isCacheValid() {
            return TileEntityDayBarrel.this.isCacheValid();
        }
    }

    public class InsertionHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = item;
            int cutoff = getMaxItemCount() - stack.getMaxStackSize();
            if (stack.isEmpty() || stack.getCount() < cutoff) {
                return ItemStack.EMPTY;
            } else {
                stack = stack.copy();
                stack.setCount(stack.getCount() - cutoff);
                if (stack.getCount() > stack.getMaxStackSize()) {
                    // ???
                    stack.setCount(stack.getMaxStackSize());
                }
            }
            return stack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack is, boolean simulate) {
            return TileEntityDayBarrel.this.insertItem(is, simulate, false);
        }
    }

    public class ReadableItemHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (item.isEmpty())
                return ItemStack.EMPTY;

            int count = item.getCount() - (slot * item.getMaxStackSize());
            if (count <= 0)
                return ItemStack.EMPTY;
            else if (count > item.getMaxStackSize())
                count = item.getMaxStackSize();

            ItemStack stack = item.copy();
            stack.setCount(count);
            return stack;
        }

        @Override
        public int getSlots() {
            return getMaxStacks();
        }

        @Override
        public int getSlotLimit(int slot) {
            return !item.isEmpty() ? item.getMaxStackSize() : 64;
        }
    }

    public class ExtractionHandler extends BaseItemHandler {
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return TileEntityDayBarrel.this.extractItem(amount, simulate, false);
        }
    }

    // TODO: Remove in 1.13
    private static final BarrelUpgrade[][] OLD_TYPE_ORDER = new BarrelUpgrade[][] {
            { },
            { BarrelUpgrade.SILKY },
            { BarrelUpgrade.HOPPING },
            { },
            { BarrelUpgrade.STICKY },
            { BarrelUpgrade.INFINITE, BarrelUpgrade.HOPPING }
    };

    private boolean updateRedstoneLevels;
    private int redstoneLevel;
    private int lastMentionedCount = -1;

    public TileEntityDayBarrel() {
        woodLog = getLog(null);
        woodSlab = getSlab(null);
    }

    private void markChunkDirty() {
        world.markChunkDirty(pos, this);
    }

    public static void populateUpgrades(Set<BarrelUpgrade> upgrades, NBTTagCompound compound) {
        if (compound.hasKey("upgrades", Constants.NBT.TAG_LIST)) {
            NBTTagList upgradeNBT = compound.getTagList("upgrades", Constants.NBT.TAG_STRING);
            for (int i = 0; i < upgradeNBT.tagCount(); i++) {
                try {
                    BarrelUpgrade type = BarrelUpgrade.valueOf(upgradeNBT.getStringTagAt(i));
                    upgrades.add(type);
                } catch (IllegalArgumentException e) {

                }
            }
        } else if (compound.hasKey("type", Constants.NBT.TAG_STRING)) {
            String s = compound.getString("type").toUpperCase();
            BarrelUpgrade type = null;
            try {
                type = BarrelUpgrade.valueOf(s);
            } catch (IllegalArgumentException e) {

            }

            if (s.equals("CREATIVE")) {
                upgrades.add(BarrelUpgrade.HOPPING);
                upgrades.add(BarrelUpgrade.INFINITE);
            } else if (type != null) {
                upgrades.add(type);
            }
        } else if (compound.hasKey("type")) {
            upgrades.addAll(Arrays.asList(OLD_TYPE_ORDER[compound.getByte("type")]));
        }
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        Orientation oldOrientation = orientation;

        item = new ItemStack(compound.getCompoundTag("item"));
        item.setCount(compound.getInteger("count"));
        orientation = Orientation.getOrientation(compound.getByte("dir"));
        helperTop = null;
        helperBottom = null;

        upgrades.clear();
        populateUpgrades(upgrades, compound);

        if (!isClient && compound.hasKey("lock", Constants.NBT.TAG_COMPOUND)) {
            lockable.deserializeNBT(compound.getCompoundTag("lock"));
        }

        woodLog = getLog(compound);
        woodSlab = getSlab(compound);
        lastMentionedCount = getItemCount();

        if (isClient && orientation != oldOrientation) {
            markBlockForRenderUpdate();
        }
    }

    @Override
    public void invalidate(InvalidationType type) {
        super.invalidate(type);
        woodLogAccess = null;
        helperTop = null;
        helperBottom = null;
    }

    @Override
    public void validate() {
        super.validate();
        updateRedstoneLevels = true;
        woodLogAccess = new ProxiedBlockAccess(getWorld()) {
            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                return getPos().equals(pos) ? null : access.getTileEntity(pos);
            }

            @Override
            public IBlockState getBlockState(BlockPos pos) {
                return getPos().equals(pos) ? ItemUtils.getBlockState(woodLog.getStack()) : access.getBlockState(pos);
            }

            @Override
            public boolean isAirBlock(BlockPos pos) {
                if (getPos().equals(pos)) {
                    IBlockState state = getBlockState(pos);
                    return state.getBlock().isAir(state, this, pos);
                } else {
                    return access.isAirBlock(pos);
                }
            }

            @Override
            public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
                return getPos().equals(pos) ? getBlockState(pos).isSideSolid(this, pos, side) : access.isSideSolid(pos, side, _default);
            }
        };

        if (!isEntity)
            Scheduler.INSTANCE.in(getWorld(), 1, this::updateComparators);
    }

    public void updateRedstoneLevel() {
        redstoneLevel = 0;
        for (EnumFacing d : EnumFacing.VALUES) {
            if (isTop(d) || isTop(d.getOpposite()))
                continue;

            redstoneLevel = Math.max(redstoneLevel, RedstoneUtils.getRedstonePower(world, pos.offset(d), d));
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        ItemUtils.writeToNBT(item, compound, "item");
        compound.setInteger("count", item.getCount());

        woodLog.writeToNBT(compound, "log");
        woodSlab.writeToNBT(compound, "slab");
        compound.setByte("dir", (byte) orientation.ordinal());

        NBTTagList upgradeNBT = new NBTTagList();
        for (BarrelUpgrade u : upgrades) {
            upgradeNBT.appendTag(new NBTTagString(u.name()));
        }
        compound.setTag("upgrades", upgradeNBT);

        if (!isClient) {
            compound.setTag("lock", lockable.serializeNBT());
        }
        return compound;
    }

    private boolean scheduledTick = true;

    public int getLogicSpeed() {
        return 8;
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        if (updateRedstoneLevels) {
            updateRedstoneLevel();
            updateRedstoneLevels = false;
        }

        if (redstoneLevel > 0 || !scheduledTick || (getWorld().getTotalWorldTime() % getLogicSpeed()) != 0) {
            return;
        }

        tick();
    }

    private void onItemChange(boolean typeChanged) {
        sync();
        if (!isEntity)
            updateComparators();
        markChunkDirty();
    }

    void tick() {
        if (!upgrades.contains(BarrelUpgrade.HOPPING) || orientation == null) {
            return;
        }
        if (notice_target == this && world.getStrongPower(pos) > 0) {
            return;
        }

        boolean itemChanged = false;

        if (helperTop == null) {
            helperTop = new CapabilityCache.Single<>(world, getPos().offset(orientation.top), false, true, true, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation.top.getOpposite());
            helperBottom = new CapabilityCache.Single<>(world, getPos().offset(orientation.top.getOpposite()), false, true, true, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation.top);
        }

        if (getItemCount() < getMaxItemCount()) {
            IItemHandler handler = helperTop.get();

            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack got = handler.extractItem(i, 1, true);
                    if (insertionView.insertItem(0, got, true).isEmpty()) {
                        got = handler.extractItem(i, 1, false);
                        insertionView.insertItem(0, got, false);
                        itemChanged = true;
                    }
                }
            }
        }

        if (getExtractableItemCount() > 0) {
            IItemHandler handler = helperBottom.get();

            if (handler != null) {
                ItemStack toPush = item.copy();
                toPush.setCount(1);
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack got = handler.insertItem(i, toPush, false);
                    if (got.isEmpty()) {
                        if (!upgrades.contains(BarrelUpgrade.INFINITE)) {
                            item.shrink(1);
                        }
                        itemChanged = true;
                        break;
                    }
                }
            }
        }

        if (itemChanged) {
            markDirty();
        }
    }

    private void needLogic() {
        scheduledTick = true;
    }

    public void neighborChanged(BlockPos pos, BlockPos fromPos) {
        if (!upgrades.contains(BarrelUpgrade.HOPPING)) {
            updateRedstoneLevel();
            // X/Z can be equal, as we only care about top/bottom neighbors for this
            if (pos.getX() == fromPos.getX() && pos.getZ() == fromPos.getZ()) {
                needLogic();
            }
        } else if (helperTop != null) {
            helperTop.neighborChanged(fromPos);
            helperBottom.neighborChanged(fromPos);
        }
    }

    @Override
    public int getItemCount() {
        if (item.isEmpty()) {
            return 0;
        } else if (upgrades.contains(BarrelUpgrade.INFINITE)) {
            return ((getMaxStacks() + 1) / 2) * item.getMaxStackSize();
        } else {
            return item.getCount();
        }
    }

    @Override
    public ItemStack extractItem(int maxCount, boolean simulate) {
        return extractItem(maxCount, simulate, true);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return insertItem(stack, simulate, true);
    }

    private ItemStack extractItem(int maxCount, boolean simulate, boolean ignoreMaxStackSize) {
        if (!item.isEmpty()) {
            int amt = Math.min(maxCount, getExtractableItemCount());
            /* if (!ignoreMaxStackSize) {
                amt = Math.min(amt, item.getMaxStackSize());
            } */
            if (amt > 0) {
                ItemStack stack = item.copy();
                stack.setCount(amt);
                if (!simulate && !upgrades.contains(BarrelUpgrade.INFINITE)) {
                    item.shrink(amt);
                    onItemChange(item.isEmpty());
                }
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack insertItem(ItemStack is, boolean simulate, boolean ignoreMaxStackSize) {
        if (is.isEmpty() || !canInsert(is)) {
            return is;
        }

        if (upgrades.contains(BarrelUpgrade.INFINITE) && !item.isEmpty()) {
            return is;
        }

        int inserted = Math.min(getMaxItemCount() - item.getCount(), is.getCount());

        if (!simulate) {
            if (item.isEmpty()) {
                item = is.copy();
                item.setCount(inserted);
                onItemChange(true);
            } else {
                item.grow(inserted);
                onItemChange(false);
            }
        }

        if (inserted == is.getCount()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack leftover = is.copy();
            leftover.shrink(inserted);
            return leftover;
        }
    }

    public int getExtractableItemCount() {
        if (item.isEmpty()) {
            return 0;
        } else if (upgrades.contains(BarrelUpgrade.INFINITE)) {
            return item.getMaxStackSize();
        } else if (upgrades.contains(BarrelUpgrade.STICKY)) {
            return Math.min(item.getCount() - 1, item.getMaxStackSize());
        } else {
            return Math.min(item.getCount(), item.getMaxStackSize());
        }
    }

    public int getMaxStacks() {
        int multiplier = 1;
        if (!item.isEmpty() && CharsetStorageBarrels.stackSizeMultiplierMap.containsKey(item.getItem())) {
            multiplier = CharsetStorageBarrels.stackSizeMultiplierMap.get(item.getItem());
        }

        return 64 * multiplier;
    }

    public int getStackDivisor() {
        int multiplier = 1;
        if (!item.isEmpty() && CharsetStorageBarrels.stackDivisorMultiplierMap.containsKey(item.getItem())) {
            multiplier = CharsetStorageBarrels.stackDivisorMultiplierMap.get(item.getItem());
        }

        return item.getMaxStackSize() * multiplier;
    }

    public int getMaxDropAmount() {
        return CharsetStorageBarrels.maxDroppedStacks * item.getMaxStackSize();
    }

    @Override
    public int getMaxItemCount() {
        if (!item.isEmpty()) {
            return item.getMaxStackSize() * getMaxStacks();
        } else {
            return 64 * getMaxStacks();
        }
    }

    @Override
    public boolean containsUpgrade(String upgradeName) {
        return upgrades.contains(BarrelUpgrade.valueOf(upgradeName));
    }

    public boolean itemMatch(ItemStack is) {
        if (is.isEmpty() || item.isEmpty()) {
            return false;
        }
        return ItemUtils.canMerge(item, is);
    }

    boolean canInsert(ItemStack is) {
        if (is.isEmpty() || isNested(is)) {
            return false;
        }
        if (item.isEmpty()) {
            return true;
        }
        return ItemUtils.canMerge(item, is);
    }

    boolean isTop(EnumFacing d) {
        return orientation != null && d == orientation.top;
    }

    boolean isTopOrBack(EnumFacing d) {
        return orientation != null && (d == orientation.top || d == orientation.facing.getOpposite());
    }

    boolean isBottom(EnumFacing d) {
        return orientation != null && d == orientation.top.getOpposite();
    }

	boolean isFront(EnumFacing d) {
		return orientation != null && d == orientation.facing;
	}

    boolean isBack(EnumFacing d) {
        return orientation != null && d == orientation.facing.getOpposite();
    }

    private boolean spammed = false;

    @Override
    public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
        orientation = SpaceUtils.getOrientation(getWorld(), getPos(), placer, face, hitX, hitY, hitZ);

        loadFromStack(stack);
        needLogic();
    }

    public void loadFromStack(ItemStack is) {
        woodLog = getLog(is.getTagCompound());
        woodSlab = getSlab(is.getTagCompound());
        upgrades.clear();
        if (is.hasTagCompound()) {
            populateUpgrades(upgrades, is.getTagCompound());
        }
        if (upgrades.contains(BarrelUpgrade.SILKY) && is.hasTagCompound()) {
            NBTTagCompound tag = is.getTagCompound();
            int loadCount = tag.getInteger("SilkCount");
            if (loadCount > 0) {
                ItemStack loadItem = getSilkedItem(is);
                if (!loadItem.isEmpty()) {
                    item = loadItem;
                    item.setCount(loadCount);
                }
            }
        }
    }

    public static ItemStack getSilkedItem(ItemStack is) {
        if (is.isEmpty() || !is.hasTagCompound()) {
            return ItemStack.EMPTY;
        }
        NBTTagCompound tag = is.getTagCompound();
        if (tag.hasKey("SilkItem")) {
            return new ItemStack(is.getTagCompound().getCompoundTag("SilkItem"));
        }
        return ItemStack.EMPTY;
    }

    public static boolean isNested(ItemStack is) {
        return !getSilkedItem(is).isEmpty();
    }

    void updateCountClients() {
        CharsetStorageBarrels.packet.sendToWatching(new PacketBarrelCountUpdate(this), this);
    }

    protected void onCountUpdate(PacketBarrelCountUpdate packet) {
        item.setCount(packet.count);
    }

    //Inventory code

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
        if (upgrades.contains(BarrelUpgrade.HOPPING)) {
            needLogic();
        }
    }

    void sync() {
        int c = getItemCount();
        if (c != lastMentionedCount) {
            if (lastMentionedCount*c <= 0) {
                //One of them was 0
                markBlockForUpdate();
            } else {
                updateCountClients();
            }
            lastMentionedCount = c;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
	        if (shouldInsertToSide(facing) || shouldExtractFromSide(facing) || facing == null) {
		        return true;
	        }
        } else if (capability == Capabilities.BARREL) {
        	return !isFront(facing);
        } else if (capability == Capabilities.LOCKABLE) {
            return true;
        } else if (capability == Capabilities.AXIS_ROTATABLE) {
            return facing != null;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public boolean shouldExtractFromSide(EnumFacing side) {
        return isBottom(side);
    }

    @Override
    public boolean shouldInsertToSide(EnumFacing side) {
        return isTopOrBack(side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (shouldExtractFromSide(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(extractionView);
            } else if (shouldInsertToSide(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(insertionView);
            } else if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(readOnlyView);
            }
        } else if (capability == Capabilities.LOCKABLE) {
            return Capabilities.LOCKABLE.cast(lockable);
        } else if (capability == Capabilities.BARREL) {
            return Capabilities.BARREL.cast(this);
        } else if (capability == Capabilities.AXIS_ROTATABLE) {
            return Capabilities.AXIS_ROTATABLE.cast(this);
        }

        return super.getCapability(capability, facing);
    }

    public void clear() {
        item = ItemStack.EMPTY;
        onItemChange(true);
    }

    // Interaction
    private final WeakHashMap<EntityPlayer, Long> lastClickMap = new WeakHashMap<>();

    public EnumActionResult insertFromItemHandler(EntityPlayer player, boolean addAll) {
        boolean hadNoItem = item.isEmpty();
        int stackCount = addAll ? Integer.MAX_VALUE : 1;
        int inserted = 0;

        ItemStack heldStack = player.getHeldItemMainhand();
        if (!heldStack.isEmpty() && heldStack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = heldStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.extractItem(i, handler.getSlotLimit(i), true);
                    if (canInsert(stack)) {
                        int free = getMaxItemCount() - getItemCount();
                        if (free <= 0) {
                            info(player);
                            return EnumActionResult.FAIL;
                        }

                        int take = Math.min(free, stack.getCount());
                        stack = handler.extractItem(i, take, false);
                        if (!stack.isEmpty()) {
                            inserted += stack.getCount();
                            ItemStack remainder = insertionView.insertItem(0, stack, false);
                            if (!remainder.isEmpty()) {
                                remainder.shrink(handler.insertItem(i, remainder, false).getCount());
                                inserted -= remainder.getCount();
                            }
                            stackCount--;
                        }

                        if (stackCount <= 0) {
                            break;
                        }
                    }
                }

                if (inserted > 0 && hadNoItem) {
                    markBlockForUpdate();
                }

                return inserted > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
            }
        }

        return EnumActionResult.PASS;
    }

    //*             Left-Click         Right-Click
    //* No Shift:   Remove stack       Add item
    //* Shift:      Remove 1 item      Use item
    //* Double:                        Add all but 1 item

    public boolean activate(EntityPlayer player, EnumFacing side, EnumHand hand) {
        if (lockable.hasLock())
            return false;

        ItemStack held = player.getHeldItem(hand);
        if (!world.isRemote && isNested(held) && (item.isEmpty() || itemMatch(held))) {
            new Notice(notice_target, new TextComponentTranslation("notice.charset.barrel.no")).sendTo(player);
            return true;
        }

        Long lastClick = lastClickMap.get(player);
        if (lastClick != null && world.getTotalWorldTime() - lastClick < 10 && !item.isEmpty()) {
            addAllItems(player, hand);
            return true;
        }
        lastClickMap.put(player, world.getTotalWorldTime());

        // right click: put an item
        if (held.isEmpty()) {
            info(player);
            return true;
        }

        switch (insertFromItemHandler(player, false)) {
            case PASS:
                break;
            case SUCCESS:
                return true;
            case FAIL:
                info(player);
                return true;
        }

        if (!canInsert(held)) {
            info(player);
            return true;
        }

        boolean hadNoItem = item.isEmpty();

        int free = getMaxItemCount() - getItemCount();
        if (free <= 0) {
            info(player);
            return true;
        }

        int take = Math.min(free, held.getCount());
        if (take > 0) {
            ItemStack toInsert = held.copy();
            toInsert.setCount(take);
            ItemStack leftover = insertionView.insertItem(0, toInsert, false);
            take -= leftover.getCount();
            if (take > 0) {
                held.shrink(take);
                if (hadNoItem) {
                    markBlockForUpdate();
                }
            } else {
                info(player);
            }
        }
        return true;
    }

    void addAllItems(EntityPlayer entityplayer, EnumHand hand) {
        switch (insertFromItemHandler(entityplayer, true)) {
            case PASS:
                break;
            case SUCCESS:
                return;
            case FAIL:
                info(entityplayer);
                return;
        }

        ItemStack held = entityplayer.getHeldItem(hand);
        InventoryPlayer inv = entityplayer.inventory;
        int total_delta = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            int free_space = getMaxItemCount() - (getItemCount() + total_delta);
            if (free_space <= 0) {
                break;
            }
            ItemStack is = inv.getStackInSlot(i);
            if (is.isEmpty() || !itemMatch(is)) {
                continue;
            }
            int toAdd = Math.min(is.getCount(), free_space);
            if (is == held && toAdd > 1) {
                toAdd -= 1;
            }
            total_delta += toAdd;
            is.shrink(toAdd);
        }

        if (total_delta > 0) {
            item.grow(total_delta);
            onItemChange(false);
            entityplayer.inventory.markDirty();
        }
    }

    public void click(EntityPlayer player) {
        if (lockable.hasLock())
            return;

        EnumHand hand = EnumHand.MAIN_HAND;

        if (item.isEmpty()) {
            info(player);
            return;
        }

        ItemStack origHeldItem = player.getHeldItem(hand);
        if (ForgeHooks.canToolHarvestBlock(world, pos, origHeldItem)) {
            return;
        }

        int removeCount = Math.min(item.getMaxStackSize(), getItemCount());
        if (removeCount <= 0)
            return;

        if (player.isSneaking()) {
            removeCount = 1;
        } else if (removeCount == getItemCount() && removeCount > 1) {
            removeCount--;
        }

        BlockPos dropPos = getPos();
        RayTraceResult result = RayTraceUtils.getCollision(getWorld(), getPos(), player, Block.FULL_BLOCK_AABB);
        if (result != null && result.sideHit != null) {
            dropPos = dropPos.offset(result.sideHit);
        } else {
            dropPos = dropPos.offset(orientation.facing);
        }

        if (upgrades.contains(BarrelUpgrade.INFINITE)) {
            if (player.isSneaking()) {
                item = ItemStack.EMPTY;
                onItemChange(true);
            } else {
                giveOrSpawnItem(player, dropPos, removeCount);
            }
        } else {
            giveOrSpawnItem(player, dropPos, removeCount);
            item.shrink(removeCount);
            onItemChange(false);
        }
    }

    protected void giveOrSpawnItem(EntityPlayer player, BlockPos dropPos, int removeCount) {
        ItemStack stack = makeStack(removeCount);

        ItemStack heldStack = player.getHeldItemMainhand();
        if (!heldStack.isEmpty() && heldStack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = heldStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                stack = ItemHandlerHelper.insertItem(handler, stack, false);
            }
        }

        if (!stack.isEmpty()) {
            ItemUtils.giveOrSpawnItemEntity(player, world, new Vec3d(dropPos).addVector(0.5, 0.5, 0.5), stack, 0.2f, 0.2f, 0.2f, 1);
        }
    }

    void info(final EntityPlayer entityplayer) {
        new Notice(notice_target, msg -> {
            if (item.isEmpty()) {
                msg.setMessage(new TextComponentTranslation("notice.charset.barrel.empty"));
            } else {
                String countMsg;
                if (upgrades.contains(BarrelUpgrade.INFINITE)) {
                    countMsg = "notice.charset.barrel.infinite";
                } else {
                    int count = getItemCount();
                    if (count >= getMaxItemCount()) {
                        countMsg = "notice.charset.barrel.full";
                    } else {
                        countMsg = "" + count;
                    }
                }
                msg.withItem(item).setMessage(new TextComponentTranslation("%1$s %2$s", new TextComponentTranslation(countMsg), new TextComponentString("{ITEM_NAME}{ITEM_INFOS_NL}")));
            }
        }).sendTo(entityplayer);
    }

    private ItemStack makeStack(int count) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack ret = item.copy();
        ret.setCount(count);
        assert ret.getCount() > 0 && ret.getCount() <= item.getMaxStackSize();
        return ret;
    }

    //Misc junk
    @Override
    public int getComparatorValue() {
        int count = getItemCount();
        if (count <= 0) {
            return 0;
        }
        int max = getMaxItemCount();
        if (count >= max) {
            return 15;
        }
        float v = count/(float)max;
        return (int) Math.max(1, Math.floor(v*15));
    }

    public List<ItemStack> getContentDrops(boolean silkTouch) {
        List<ItemStack> stacks = new ArrayList<>();

        if (upgrades.contains(BarrelUpgrade.INFINITE) || (upgrades.contains(BarrelUpgrade.SILKY) && silkTouch)) {
            return stacks;
        }

        if (item.isEmpty()) {
            return stacks;
        }

        int count = Math.min(getItemCount(), getMaxDropAmount());
        if (count <= 0) {
            return stacks;
        }

        int prev_count = 0;
        while (prev_count != count && count > 0) {
            int toDrop = Math.min(item.getMaxStackSize(), count);
            ItemStack dropStack = item.copy();
            dropStack.setCount(toDrop);
            stacks.add(dropStack);
            prev_count = count;
            count -= toDrop;
        }

        return stacks;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> stacks, IBlockState state, int fortune, boolean silkTouch) {
        stacks.add(getDroppedBlock(silkTouch));
        stacks.addAll(getContentDrops(silkTouch));
    }

    public boolean canLose() {
        return !item.isEmpty() && !upgrades.contains(BarrelUpgrade.INFINITE) && getItemCount() > getMaxDropAmount();
    }

    public static ItemStack makeDefaultBarrel(Set<BarrelUpgrade> upgrades) {
        return makeBarrel(upgrades, ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("log"), ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("slab"));
    }

    public static ItemStack makeBarrel(Set<BarrelUpgrade> upgrades, ItemMaterial log, ItemMaterial slab) {
        ItemStack stack = new ItemStack(CharsetStorageBarrels.barrelItem);
        NBTTagCompound compound = ItemUtils.getTagCompound(stack, true);
        compound.setString("log", log.getId());
        compound.setString("slab", slab.getId());
        if (upgrades.size() > 0) {
            NBTTagList list = new NBTTagList();
            for (BarrelUpgrade u : upgrades)
                list.appendTag(new NBTTagString(u.name()));
            compound.setTag("upgrades", list);
        }
        return stack;
    }

    public static ItemMaterial getLog(NBTTagCompound tag) {
        return ItemMaterialRegistry.INSTANCE.getMaterial(tag, "log", "log");
    }

    public static ItemMaterial getSlab(NBTTagCompound tag) {
        return ItemMaterialRegistry.INSTANCE.getMaterial(tag, "slab", "slab");
    }

    // TODO: Handle invalid combinations somewhere?
    static ItemStack addUpgrade(ItemStack barrel, BarrelUpgrade upgrade) {
        NBTTagCompound tag = ItemUtils.getTagCompound(barrel, true);
        NBTTagList list;
        if (!tag.hasKey("upgrades")) {
            list = new NBTTagList();
        } else {
            list = tag.getTagList("upgrades", Constants.NBT.TAG_STRING);
            for (int i = 0; i < list.tagCount(); i++) {
                if (list.getStringTagAt(i).equals(upgrade.name())) {
                    return barrel;
                }
            }
        }

        list.appendTag(new NBTTagString(upgrade.name()));
        if (list.tagCount() > 0) {
            tag.setTag("upgrades", list);
        }
        return barrel;
    }

    private boolean changeOrientation(Orientation newOrientation, boolean simulate) {
        if (orientation != newOrientation) {
            if (!simulate) {
                orientation = newOrientation;
                helperTop = null;
                helperBottom = null;
                markBlockForUpdate();
                getWorld().notifyNeighborsRespectDebug(pos, getBlockType(), true);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mirror(Mirror mirror) {
        changeOrientation(orientation.mirror(mirror), false);
    }

    @Override
    public boolean rotateAround(EnumFacing axis, boolean simulate) {
        return changeOrientation(orientation.rotateAround(axis), simulate);
    }

    @Override
    public boolean rotateWrench(EnumFacing axis) {
        Orientation newOrientation;
        if (axis == orientation.facing) {
            newOrientation = orientation.getNextRotationOnFace();
        } else {
            newOrientation = Orientation.getOrientation(Orientation.fromDirection(axis).ordinal() & (~3) | (orientation.ordinal() & 3));
        }

        changeOrientation(newOrientation, false);
        return true;
    }

    @Override
    public ItemStack getPickedBlock(@Nullable EntityPlayer player, @Nullable RayTraceResult result, IBlockState state) {
        if (player == null || !player.isSneaking()) {
            return getDroppedBlock(state);
        } else {
            ItemStack stack = item.copy();
            if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
                stack.setCount(stack.getMaxStackSize());
            }
            return stack;
        }
    }

    @Override
    public ItemStack getDroppedBlock(IBlockState state) {
        return getDroppedBlock(false);
    }

    public ItemStack getDroppedBlock(boolean silkTouch) {
        ItemStack is = makeBarrel(upgrades, woodLog, woodSlab);
        if (upgrades.contains(BarrelUpgrade.SILKY) && !item.isEmpty() && silkTouch) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                is.setTagCompound(tag);
            }
            tag.setInteger("SilkCount", getItemCount());
            NBTTagCompound si = new NBTTagCompound();
            item.writeToNBT(si);
            tag.setTag("SilkItem", si);
        }
        return is;
    }

    public boolean canHarvest(EntityPlayer player) {
        if (item.isEmpty()) {
            return true;
        }

        if (player == null || !player.isCreative()) {
            return true;
        }

        // Propagate left click
        if (player.world.isRemote) {
            player.swingArm(EnumHand.MAIN_HAND);
        } else {
            click(player);
        }

        return false;
    }

    public boolean isWooden() {
        try {
            return woodLog.getTypes().contains("wood");
        } catch (Exception e) {
            return false;
        }
    }

    public int getFlamability(EnumFacing face) {
        try {
            return ItemUtils.getBlockState(woodLog.getStack()).getBlock().getFlammability(woodLogAccess, getPos(), face);
        } catch (Exception e) {
            return isWooden() ? 20 : 0;
        }
    }

    public boolean isFlammable(EnumFacing face) {
        try {
            return ItemUtils.getBlockState(woodLog.getStack()).getBlock().isFlammable(woodLogAccess, getPos(), face);
        } catch (Exception e) {
            return isWooden();
        }
    }

    public int getFireSpreadSpeed(EnumFacing face) {
        try {
            return ItemUtils.getBlockState(woodLog.getStack()).getBlock().getFireSpreadSpeed(woodLogAccess, getPos(), face);
        } catch (Exception e) {
            return isWooden() ? 5 : 0;
        }
    }

    public SoundType getSoundType() {
        try {
            if (isTop(EnumFacing.UP) || isBottom(EnumFacing.UP)) {
                return ItemUtils.getBlockState(woodSlab.getStack()).getBlock().getSoundType();
            } else {
                return ItemUtils.getBlockState(woodLog.getStack()).getBlock().getSoundType();
            }
        } catch (Exception e) {
            return SoundType.WOOD;
        }
    }
}
