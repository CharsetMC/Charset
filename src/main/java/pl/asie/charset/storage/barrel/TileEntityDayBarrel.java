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

package pl.asie.charset.storage.barrel;

import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.INoticeUpdater;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.storage.ModCharsetStorage;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class TileEntityDayBarrel extends TileBase implements ITickable {
    public ItemStack item = ItemStack.EMPTY;
    private static final ItemStack DEFAULT_LOG = new ItemStack(Blocks.LOG);
    private static final ItemStack DEFAULT_SLAB = new ItemStack(Blocks.PLANKS);
    public ItemStack woodLog = DEFAULT_LOG, woodSlab = DEFAULT_SLAB;
    // TODO: Dynamic barrel sizes!

    public Orientation orientation = Orientation.FACE_UP_POINT_NORTH;
    public Type type = Type.NORMAL;
    Object notice_target = this;

    private static final int maxStackDrop = 64*64*2;
    protected final InsertionHandler insertionView = new InsertionHandler();
    protected final ExtractionHandler extractionView = new ExtractionHandler();
    protected final ReadableItemHandler readOnlyView = new ReadableItemHandler();

    public abstract class BaseItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Math.min(64, getMaxSize());
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    public class InsertionHandler extends BaseItemHandler {
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
        public ItemStack insertItem(int slot, ItemStack is, boolean simulate) {
            if (is.isEmpty() || !canInsert(is)) {
                return is;
            }

            if (type == Type.CREATIVE && !item.isEmpty()) {
                return is;
            }

            int inserted = Math.min(getMaxSize() - item.getCount(), is.getCount());

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
    }

    public class ReadableItemHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = item.copy();
            if (stack.getCount() > 64)
                stack.setCount(64);
            return stack;
        }
    }

    public class ExtractionHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = item.copy();
            if (type == Type.STICKY)
                stack.shrink(1);
            if (stack.getCount() > 64)
                stack.setCount(64);
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!item.isEmpty()) {
                int amt = Math.min(amount, getExtractableItemCount());
                if (amt > 0) {
                    ItemStack stack = item.copy();
                    stack.setCount(amt);
                    if (!simulate && type != Type.CREATIVE) {
                        item.shrink(amt);
                        onItemChange(item.isEmpty());
                    }
                    return stack;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    public enum Type {
        NORMAL, SILKY, HOPPING, LARGER, STICKY, CREATIVE;

        public static final Type[] VALUES = values();
        public static Type valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return NORMAL;
            }
            return VALUES[ordinal];
        }

        public boolean isHopping() {
            return this == HOPPING || this == CREATIVE;
        }
    }
    private int last_mentioned_count = -1;

    private void markChunkDirty() {
        world.markChunkDirty(pos, this);
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        Orientation oldOrientation = orientation;

        woodLog = DEFAULT_LOG;
        woodSlab = DEFAULT_SLAB;

        item = new ItemStack(compound.getCompoundTag("item"));
        item.setCount(compound.getInteger("count"));
        orientation = Orientation.getOrientation(compound.getByte("dir"));
        type = Type.VALUES[compound.getByte("type")];
        if (compound.hasKey("log")) {
            woodLog = ItemUtils.firstNonEmpty(new ItemStack(compound.getCompoundTag("log")), DEFAULT_LOG);
        }
        if (compound.hasKey("slab")) {
            woodSlab = ItemUtils.firstNonEmpty(new ItemStack(compound.getCompoundTag("slab")), DEFAULT_SLAB);
        }

        last_mentioned_count = getItemCount();

        if (isClient && orientation != oldOrientation)
            markBlockForRenderUpdate();
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        ItemUtils.writeToNBT(item, compound, "item");
        if (!woodLog.isEmpty()) {
            ItemUtils.writeToNBT(woodLog, compound, "log");
        }
        if (!woodSlab.isEmpty()) {
            ItemUtils.writeToNBT(woodSlab, compound, "slab");
        }
        compound.setByte("dir", (byte) orientation.ordinal());
        compound.setByte("type", (byte) type.ordinal());
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

        if (!scheduledTick || (getWorld().getTotalWorldTime() % getLogicSpeed()) != 0) {
            return;
        }

        tick();
    }

    private void onItemChange(boolean typeChanged) {
        sync();
        markChunkDirty();
    }

    void tick() {
        if (!type.isHopping() || orientation == null) {
            return;
        }
        if (notice_target == this && world.getStrongPower(pos) > 0) {
            return;
        }

        boolean itemChanged = false;

        if (getItemCount() < getMaxSize()) {
            BlockPos upPos = getPos().offset(orientation.top);
            IItemHandler handler = CapabilityHelper.get(getWorld(), upPos,
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation.top.getOpposite(),
                    true, true);

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
            BlockPos downPos = getPos().offset(orientation.top.getOpposite());
            IItemHandler handler = CapabilityHelper.get(getWorld(), downPos,
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation.top,
                    true, true);

            if (handler != null) {
                ItemStack toPush = item.copy();
                toPush.setCount(1);
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack got = handler.insertItem(i, toPush, false);
                    if (got.isEmpty()) {
                        item.shrink(1);
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
        // X/Z can be equal, as we only care about top/bottom neighbors
        if (type.isHopping() && pos.getX() == fromPos.getX() && pos.getZ() == fromPos.getZ()) {
            needLogic();
        }
    }

    public int getItemCount() {
        if (item.isEmpty()) {
            return 0;
        } else if (type == Type.CREATIVE) {
            return 32*item.getMaxStackSize();
        } else {
            return item.getCount();
        }
    }

    public int getExtractableItemCount() {
        if (item.isEmpty()) {
            return 0;
        } else if (type == Type.CREATIVE) {
            return item.getMaxStackSize();
        } else if (type == Type.STICKY) {
            return Math.min(item.getCount() - 1, item.getMaxStackSize());
        } else {
            return Math.min(item.getCount(), item.getMaxStackSize());
        }
    }

    public int getMaxSize() {
        int size = 64*64;
        if (!item.isEmpty()) {
            size = item.getMaxStackSize()*64;
        }
        if (type == Type.LARGER) {
            size *= 2;
        }
        return size;
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
        return d == orientation.top;
    }

    boolean isTopOrBack(EnumFacing d) {
        return d == orientation.top || d == orientation.facing.getOpposite();
    }

    boolean isBottom(EnumFacing d) {
        return d == orientation.top.getOpposite();
    }

    boolean isBack(EnumFacing d) {
        return d == orientation.facing.getOpposite();
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
        woodLog = getLog(is);
        woodSlab = getSlab(is);
        type = getUpgrade(is);
        if (type == Type.SILKY && is.hasTagCompound()) {
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
        if (is .isEmpty() || !is.hasTagCompound()) {
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
        ModCharsetStorage.packet.sendToWatching(new PacketBarrelCountUpdate(this), this);
    }

    protected void onCountUpdate(PacketBarrelCountUpdate packet) {
        item.setCount(packet.count);
    }

    //Inventory code

    @Override
    public void markDirty() {
        super.markDirty();
        sync();
        if (type.isHopping()) {
            needLogic();
        }
    }

    void sync() {
        int c = getItemCount();
        if (c != last_mentioned_count) {
            if (last_mentioned_count*c <= 0) {
                //One of them was 0
                markBlockForUpdate();
            } else {
                updateCountClients();
            }
            last_mentioned_count = c;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return isTop(facing) || isBottom(facing) || facing == null;
        } else {
            return false;
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (isBottom(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(extractionView);
            } else if (isTop(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(insertionView);
            } else if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(readOnlyView);
            }
        }

        return null;
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

    public boolean activate(EntityPlayer entityplayer, EnumFacing side, EnumHand hand) {
        // right click: put an item
        Long lastClick = lastClickMap.get(entityplayer);
        if (lastClick != null && world.getTotalWorldTime() - lastClick < 10 && !item.isEmpty()) {
            addAllItems(entityplayer, hand);
            return true;
        }
        lastClickMap.put(entityplayer, world.getTotalWorldTime());

        ItemStack held = entityplayer.getHeldItem(hand);
        if (held.isEmpty()) {
            info(entityplayer);
            return true;
        }

        if (!world.isRemote && isNested(held) && (item.isEmpty() || itemMatch(held))) {
            new Notice(notice_target, "notice.charset.barrel.no").sendTo(entityplayer);
            return true;
        }

        NBTTagCompound tag = held.getTagCompound();
        if (tag != null && tag.hasKey("noFzBarrel")) {
            return false;
        }

        if (!canInsert(held)) {
            info(entityplayer);
            return true;
        }

        boolean veryNew = item.isEmpty();

        int free = getMaxSize() - getItemCount();
        if (free <= 0) {
            info(entityplayer);
            return true;
        }

        int take = Math.min(free, held.getCount());
        ItemStack leftover = insertionView.insertItem(0, held.splitStack(take), false);
        take -= leftover.getCount();
        if (take > 0) {
            held.shrink(take);
            if (veryNew) {
                markBlockForUpdate();
            }
        } else {
            info(entityplayer);
        }
        return true;
    }

    void addAllItems(EntityPlayer entityplayer, EnumHand hand) {
        ItemStack held = entityplayer.getHeldItem(hand);
        InventoryPlayer inv = entityplayer.inventory;
        int total_delta = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            int free_space = getMaxSize() - (getItemCount() + total_delta);
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

    static boolean isStairish(World w, BlockPos pos) {
        IBlockState b = w.getBlockState(pos);
        // TODO
        /* if (b.getBlock() instanceof BlockRailBase) {
            return true;
        } */
        AxisAlignedBB ab = b.getCollisionBoundingBox(w, pos);
        ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        b.addCollisionBoxToList(w, pos, ab, list, null);
        for (AxisAlignedBB bb : list) {
            if (bb.maxY - pos.getY() <= 0.51) {
                return true;
            }
        }
        return false;
    }

    boolean punt(EntityPlayer player, EnumHand hand) {
        int distance = 0; // TODO: Restore punt
        if (distance <= 0) {
            return false;
        }
        RayTraceResult result = RayTraceUtils.getCollision(getWorld(), getPos(), player, Block.FULL_BLOCK_AABB, 0);
        if (result == null || result.sideHit == null) {
            return false;
        }
        EnumFacing dir = result.sideHit.getOpposite();
        BlockPos src = getPos();
        BlockPos next = src;
        Orientation newOrientation = orientation;
        boolean doRotation = dir.getDirectionVec().getY() == 0;
        EnumFacing rotationAxis = null;
        if (doRotation) {
            rotationAxis = SpaceUtils.rotateCounterclockwise(dir, EnumFacing.UP);
        }
        if (player.isSneaking() && distance > 1) {
            distance = 1;
        }
        int spillage = distance;
        int doubleRolls = 0;
        for (int i = 0; i < distance; i++) {
            if (i > 6) {
                break;
            }
            boolean must_rise_or_fail = false;
            BlockPos peek = next.offset(dir);
            if (!getWorld().isBlockLoaded(peek)) {
                break;
            }
            IBlockState peekState = getWorld().getBlockState(peek);
            BlockPos below = peek.offset(EnumFacing.DOWN);
            int rotateCount = 1;
            if (!peekState.getBlock().isReplaceable(getWorld(), peek)) {
                if (!isStairish(getWorld(), peek)) {
                    break;
                }
                BlockPos above = peek.offset(EnumFacing.UP);
                if (!getWorld().isAirBlock(above) /* Not going to replace snow in this case */) {
                    break;
                }
                next = above;
                spillage += 3;
                rotateCount++;
                doubleRolls++;
            } else if (getWorld().getBlockState(below).getBlock().isReplaceable(getWorld(), below) && i != distance - 1) {
                next = below;
                spillage++;
            } else {
                next = peek;
            }
            if (!doRotation) {
                rotateCount = 0;
            }
            //When we roll a barrel, the side we punch should face up
            for (int r = rotateCount; r > 0; r--) {
                EnumFacing nTop = SpaceUtils.rotateCounterclockwise(newOrientation.top, rotationAxis);
                EnumFacing nFace = SpaceUtils.rotateCounterclockwise(newOrientation.facing, rotationAxis);
                newOrientation = Orientation.fromDirection(nFace).pointTopTo(nTop);
            }
        }
        if (src.equals(next)) {
            return false;
        }
        if (!doRotation && orientation.top == EnumFacing.UP && dir == EnumFacing.UP) {
            spillage = 0;
        }
        // TODO
        /* if (doubleRolls % 2 == 1) {
            Sound.barrelPunt2.playAt(src);
        } else {
            Sound.barrelPunt.playAt(src);
        } */
        getWorld().removeTileEntity(src);
        getWorld().setBlockToAir(src);
        if (newOrientation != null) {
            this.orientation = newOrientation;
        }
        this.validate();
        getWorld().setBlockState(next, ModCharsetStorage.barrelBlock.getDefaultState());
        getWorld().setTileEntity(next, this);
        player.addExhaustion(0.5F);
        ItemStack is = player.getHeldItem(hand);
        if (!is.isEmpty() && is.isItemStackDamageable() && world.rand.nextInt(4) == 0) {
            is.damageItem(distance, player);
            if (is.getCount() <= 0) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
        }
        //spillItems(spillage); // Meh!
        return true;
    }

    public void click(EntityPlayer player) {
        EnumHand hand = EnumHand.MAIN_HAND;
        // left click: remove a stack, or punt if properly equipped
        if (punt(player, hand)) {
            return;
        }

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

        ItemUtils.giveOrSpawnItemEntity(player, world, new Vec3d(dropPos).addVector(0.5, 0.5, 0.5), makeStack(removeCount), 0.2f, 0.2f, 0.2f, 1);
        item.shrink(removeCount);
        onItemChange(false);
    }

    void info(final EntityPlayer entityplayer) {
        new Notice(notice_target, new INoticeUpdater() {
            @Override
            public void update(Notice msg) {
                if (item.isEmpty()) {
                    msg.setMessage("notice.charset.barrel.empty");
                } else {
                    String countMsg = null;
                    if (type == Type.CREATIVE) {
                        countMsg = "notice.charset.barrel.infinite";
                    } else {
                        int count = getItemCount();
                        if (count >= getMaxSize()) {
                            countMsg = "notice.charset.barrel.full";
                        } else {
                            countMsg = "" + count;
                        }
                    }
                    msg.withItem(item).setMessage("%s {ITEM_NAME}{ITEM_INFOS_NEWLINE}", countMsg);
                }
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
        if (count == 0) {
            return 0;
        }
        int max = getMaxSize();
        if (count == max) {
            return 15;
        }
        float v = count/(float)max;
        return (int) Math.max(1, v*14);
    }

    @Override
    public void dropContents() {
        if (type == Type.CREATIVE || (type == Type.SILKY && broken_with_silk_touch)) {
            return;
        }
        if (item.isEmpty() || getItemCount() <= 0 ) {
            return;
        }
        int count = getItemCount();
        for (int i = 0; i < maxStackDrop; i++) {
            int to_drop;
            to_drop = Math.min(item.getMaxStackSize(), count);
            count -= to_drop;
            ItemUtils.spawnItemEntity(world, new Vec3d(getPos()).addVector(0.5, 0.5, 0.5), makeStack(to_drop), 0.2f, 0.2f, 0.2f, 1);
            if (count <= 0) {
                break;
            }
        }
    }

    public boolean canLose() {
        return item != null && getItemCount() > maxStackDrop * item.getMaxStackSize();
    }

    public static ItemStack makeBarrel(Type type, ItemStack log, ItemStack slab) {
        ItemStack barrel_item = new ItemStack(ModCharsetStorage.barrelItem);
        barrel_item = addUpgrade(barrel_item, type);
        NBTTagCompound compound = ItemUtils.getTagCompound(barrel_item, true);
        ItemUtils.writeToNBT(log, compound, "log");
        ItemUtils.writeToNBT(slab, compound, "slab");
        return barrel_item;
    }

    public static Type getUpgrade(ItemStack is) {
        if (is == null) {
            return Type.NORMAL;
        }
        NBTTagCompound tag = is.getTagCompound();
        if (tag == null) {
            return Type.NORMAL;
        }
        String name = tag.getString("type");
        if (name == null || name.equals("")) {
            return Type.NORMAL;
        }
        try {
            return Type.valueOf(name);
        } catch (IllegalArgumentException e) {
            ModCharsetStorage.logger.warn("%s has invalid barrel Type %s. Resetting it.", is, name);
            e.printStackTrace();
            tag.removeTag("type");
            return Type.NORMAL;
        }
    }

    public static ItemStack getLog(ItemStack is) {
        return get(is, "log", DEFAULT_LOG);
    }

    public static ItemStack getSlab(ItemStack is) {
        return get(is, "slab", DEFAULT_SLAB);
    }

    private static ItemStack get(ItemStack is, String name, ItemStack default_) {
        ItemStack stack = default_;
        if (is != null && is.hasTagCompound() && is.getTagCompound().hasKey(name)) {
            ItemStack stack1 = new ItemStack(is.getTagCompound().getCompoundTag(name));
            if (stack1 != null) {
                stack = stack1;
            }
        }
        return stack;
    }

    static ItemStack addUpgrade(ItemStack barrel, Type upgrade) {
        if (upgrade == Type.NORMAL) {
            return barrel;
        }
        barrel = barrel.copy();
        NBTTagCompound tag = ItemUtils.getTagCompound(barrel, true);
        tag.setString("type", upgrade.toString());
        return barrel;
    }

    public boolean canRotate(EnumFacing axis) {
        return axis != null;
    }

    public void rotate(EnumFacing axis) {
        Orientation oldOrientation = orientation;

        if (axis == orientation.facing.getOpposite()) {
            orientation = orientation.getNextRotationOnFace();
        } else {
            orientation = Orientation.getOrientation(Orientation.fromDirection(axis.getOpposite()).ordinal() & (~3) | (orientation.ordinal() & 3));
        }

        if (orientation != oldOrientation) {
            markBlockForUpdate();
            getWorld().notifyNeighborsRespectDebug(pos, getBlockType(), true);
        }
    }

    @Override
    public ItemStack getDroppedBlock() {
        ItemStack is = makeBarrel(type, woodLog, woodSlab);
        if (type == Type.SILKY && !item.isEmpty() && broken_with_silk_touch) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                is.setTagCompound(tag);
            }
            tag.setInteger("SilkCount", getItemCount());
            NBTTagCompound si = new NBTTagCompound();
            item.writeToNBT(si);
            tag.setTag("SilkItem", si);
            tag.setLong("rnd", hashCode() + world.getTotalWorldTime());
        }
        return is;
    }

    boolean broken_with_silk_touch = false;

    public boolean removedByPlayer(EntityPlayer player, boolean willHarvest) {
        if (cancelRemovedByPlayer(player)) return false;
        broken_with_silk_touch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItem(EnumHand.MAIN_HAND)) > 0;
        return true;
    }

    private boolean cancelRemovedByPlayer(EntityPlayer player) {
        if (item == null) {
            return false;
        }
        if (player == null || !player.capabilities.isCreativeMode || player.isSneaking()) {
            return false;
        }
        if (player.world.isRemote) {
            player.swingArm(EnumHand.MAIN_HAND); // TODO
        } else {
            click(player);
        }
        return true;
    }

    public boolean isWooden() {
        return ItemUtils.getBlockState(woodLog).getMaterial() == Material.WOOD;
    }

    public int getFlamability() {
        try {
            return ItemUtils.getBlockState(woodLog).getBlock().getFlammability(getWorld(), getPos(), null);
        } catch (Exception e) {
            return isWooden() ? 20 : 0;
        }
    }
}
