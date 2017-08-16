/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.api.lib.ICacheable;
import pl.asie.charset.api.storage.IBarrel;
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

public class TileEntityDayBarrel extends TileBase implements IBarrel, ICacheable, ITickable, IAxisRotatable {
    public ItemStack item = ItemStack.EMPTY;
    public ItemMaterial woodLog, woodSlab;
    public Orientation orientation = Orientation.FACE_UP_POINT_NORTH;
    public Set<Upgrade> upgrades = EnumSet.noneOf(Upgrade.class);
    Object notice_target = this;

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
            if (stack.getCount() > 64) {
                stack = stack.copy();
                stack.setCount(64);
            }
            return stack;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
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
            else if (count > 64)
                count = 64;

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

    public enum Upgrade {
        SILKY, HOPPING, STICKY, INFINITE;

        public static final Upgrade[] VALUES = values();
    }

    // TODO: Remove in 1.13
    private static final Upgrade[][] OLD_TYPE_ORDER = new Upgrade[][] {
            { },
            { Upgrade.SILKY },
            { Upgrade.HOPPING },
            { },
            { Upgrade.STICKY },
            { Upgrade.INFINITE, Upgrade.HOPPING }
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

    public static void populateUpgrades(Set<Upgrade> upgrades, NBTTagCompound compound) {
        if (compound.hasKey("upgrades", Constants.NBT.TAG_LIST)) {
            NBTTagList upgradeNBT = compound.getTagList("upgrades", Constants.NBT.TAG_STRING);
            for (int i = 0; i < upgradeNBT.tagCount(); i++) {
                try {
                    Upgrade type = Upgrade.valueOf(upgradeNBT.getStringTagAt(i));
                    upgrades.add(type);
                } catch (IllegalArgumentException e) {

                }
            }
        } else if (compound.hasKey("type", Constants.NBT.TAG_STRING)) {
            String s = compound.getString("type").toUpperCase();
            Upgrade type = null;
            try {
                type = Upgrade.valueOf(s);
            } catch (IllegalArgumentException e) {

            }

            if (s.equals("CREATIVE")) {
                upgrades.add(Upgrade.HOPPING);
                upgrades.add(Upgrade.INFINITE);
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

        woodLog = getLog(compound);
        woodSlab = getSlab(compound);
        lastMentionedCount = getItemCount();

        if (isClient && orientation != oldOrientation) {
            markBlockForRenderUpdate();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
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
        woodLog.writeToNBT(compound, "log");
        woodSlab.writeToNBT(compound, "slab");
        compound.setByte("dir", (byte) orientation.ordinal());

        NBTTagList upgradeNBT = new NBTTagList();
        for (Upgrade u : upgrades) {
            upgradeNBT.appendTag(new NBTTagString(u.name()));
        }
        compound.setTag("upgrades", upgradeNBT);
        compound.setInteger("count", item.getCount());
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
        if (!upgrades.contains(Upgrade.HOPPING) || orientation == null) {
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
                        if (!upgrades.contains(Upgrade.INFINITE)) {
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
        if (!upgrades.contains(Upgrade.HOPPING)) {
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
        } else if (upgrades.contains(Upgrade.INFINITE)) {
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
                if (!simulate && !upgrades.contains(Upgrade.INFINITE)) {
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

        if (upgrades.contains(Upgrade.INFINITE) && !item.isEmpty()) {
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
        } else if (upgrades.contains(Upgrade.INFINITE)) {
            return item.getMaxStackSize();
        } else if (upgrades.contains(Upgrade.STICKY)) {
            return Math.min(item.getCount() - 1, item.getMaxStackSize());
        } else {
            return Math.min(item.getCount(), item.getMaxStackSize());
        }
    }

    public int getMaxStacks() {
        return 64;
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
        return upgrades.contains(Upgrade.valueOf(upgradeName));
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

    boolean isBack(EnumFacing d) {
        return orientation != null && d == orientation.facing.getOpposite();
    }

    private boolean spammed = false;

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        RayTraceResult hit = RayTraceUtils.getCollision(getWorld(), getPos(), placer, Block.FULL_BLOCK_AABB, 0);
        if (hit != null) {
            if (hit.hitVec != null) {
                orientation = SpaceUtils.getOrientation(placer, hit.sideHit, hit.hitVec.subtract(new Vec3d(getPos())));
            } else if (hit.sideHit != null) {
                orientation = Orientation.fromDirection(hit.sideHit);
            }
        }
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
        if (upgrades.contains(Upgrade.SILKY) && is.hasTagCompound()) {
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
        if (upgrades.contains(Upgrade.HOPPING)) {
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
            if (isTop(facing) || isBottom(facing) || facing == null) {
                return true;
            }
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
        return isTop(side);
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

    //*             Left-Click         Right-Click
    //* No Shift:   Remove stack       Add item
    //* Shift:      Remove 1 item      Use item
    //* Double:                        Add all but 1 item

    public boolean activate(EntityPlayer player, EnumFacing side, EnumHand hand) {
        Long lastClick = lastClickMap.get(player);
        if (lastClick != null && world.getTotalWorldTime() - lastClick < 10 && !item.isEmpty()) {
            addAllItems(player, hand);
            return true;
        }
        lastClickMap.put(player, world.getTotalWorldTime());

        // right click: put an item
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) {
            info(player);
            return true;
        }

        if (!world.isRemote && isNested(held) && (item.isEmpty() || itemMatch(held))) {
            new Notice(notice_target, "notice.charset.barrel.no").sendTo(player);
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
        RayTraceResult result = RayTraceUtils.getCollision(getWorld(), getPos(), player, Block.FULL_BLOCK_AABB, 0);
        if (result != null && result.sideHit != null) {
            dropPos = dropPos.offset(result.sideHit);
        } else {
            dropPos = dropPos.offset(orientation.facing);
        }

        if (upgrades.contains(Upgrade.INFINITE)) {
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
        ItemUtils.giveOrSpawnItemEntity(player, world, new Vec3d(dropPos).addVector(0.5, 0.5, 0.5), makeStack(removeCount), 0.2f, 0.2f, 0.2f, 1);
    }

    void info(final EntityPlayer entityplayer) {
        new Notice(notice_target, msg -> {
            if (item.isEmpty()) {
                msg.setMessage("notice.charset.barrel.empty");
            } else {
                String countMsg;
                if (upgrades.contains(Upgrade.INFINITE)) {
                    countMsg = "notice.charset.barrel.infinite";
                } else {
                    int count = getItemCount();
                    if (count >= getMaxItemCount()) {
                        countMsg = "notice.charset.barrel.full";
                    } else {
                        countMsg = "" + count;
                    }
                }
                msg.withItem(item).setMessage("%s {ITEM_NAME}{ITEM_INFOS_NEWLINE}", countMsg);
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

        if (upgrades.contains(Upgrade.INFINITE) || (upgrades.contains(Upgrade.SILKY) && silkTouch)) {
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

    public List<ItemStack> getDrops(boolean silkTouch) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(getDroppedBlock(silkTouch));
        stacks.addAll(getContentDrops(silkTouch));
        return stacks;
    }

    public boolean canLose() {
        return !item.isEmpty() && !upgrades.contains(Upgrade.INFINITE) && getItemCount() > getMaxDropAmount();
    }

    public static ItemStack makeDefaultBarrel(Set<Upgrade> upgrades) {
        return makeBarrel(upgrades, ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("log"), ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("slab"));
    }

    public static ItemStack makeBarrel(Set<Upgrade> upgrades, ItemMaterial log, ItemMaterial slab) {
        ItemStack stack = new ItemStack(CharsetStorageBarrels.barrelItem);
        NBTTagCompound compound = ItemUtils.getTagCompound(stack, true);
        compound.setString("log", log.getId());
        compound.setString("slab", slab.getId());
        if (upgrades.size() > 0) {
            NBTTagList list = new NBTTagList();
            for (Upgrade u : upgrades)
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
    static ItemStack addUpgrade(ItemStack barrel, Upgrade upgrade) {
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

    @Override
    public boolean rotateAround(EnumFacing axis, boolean simulate) {
        Orientation newOrientation = orientation.rotateAround(axis);

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

    public void rotateWrench(EnumFacing axis) {
        Orientation oldOrientation = orientation;

        if (axis == orientation.facing.getOpposite()) {
            orientation = orientation.getNextRotationOnFace();
        } else {
            orientation = Orientation.getOrientation(Orientation.fromDirection(axis.getOpposite()).ordinal() & (~3) | (orientation.ordinal() & 3));
        }

        if (orientation != oldOrientation) {
            helperTop = null;
            helperBottom = null;
            markBlockForUpdate();
            getWorld().notifyNeighborsRespectDebug(pos, getBlockType(), true);
        }
    }

    @Override
    public ItemStack getDroppedBlock(IBlockState state) {
        return getDroppedBlock(false);
    }

    public ItemStack getDroppedBlock(boolean silkTouch) {
        ItemStack is = makeBarrel(upgrades, woodLog, woodSlab);
        if (upgrades.contains(Upgrade.SILKY) && !item.isEmpty() && silkTouch) {
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
