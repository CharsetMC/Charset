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

import pl.asie.charset.lib.factorization.Orientation;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.NoticeUpdater;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.utils.CapabilityUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.factorization.SpaceUtil;
import pl.asie.charset.lib.utils.PlayerUtils;
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.storage.ModCharsetStorage;

import java.util.ArrayList;

public class TileEntityDayBarrel extends TileBase implements ITickable {
    public ItemStack item;
    private ItemStack topStack;
    private int middleCount;
    private ItemStack bottomStack;
    static final ItemStack DEFAULT_LOG = new ItemStack(Blocks.LOG);
    static final ItemStack DEFAULT_SLAB = new ItemStack(Blocks.PLANKS);
    public ItemStack woodLog = DEFAULT_LOG.copy(), woodSlab = DEFAULT_SLAB.copy();
    {
        // TODO: Dynamic barrel sizes!
    }

    public Orientation orientation = Orientation.FACE_UP_POINT_NORTH;
    public Type type = Type.NORMAL;
    Object notice_target = this;

    private static final int maxStackDrop = 64*64*2;

    public abstract class BaseItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return null;
        }
    }

    public class InsertionHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            updateStacks();
            return topStack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack is, boolean simulate) {
            updateStacks();

            if (is == null || !taint(is)) {
                if (!spammed) {
                    ModCharsetStorage.logger.warn("Bye bye, %s", is);
                    Thread.dumpStack();
                    spammed = true;
                }
                return is;
            }

            if (topStack == null) {
                if (!simulate) {
                    topStack = is.copy();
                    sync();
                    markChunkDirty();
                }
                return null;
            } else {
                int inserted = Math.min(topStack.getMaxStackSize() - topStack.stackSize, is.stackSize);
                if (!simulate) {
                    topStack.stackSize += inserted;
                    sync();
                    markChunkDirty();
                }
                if (inserted == is.stackSize) {
                    return null;
                } else {
                    ItemStack leftover = is.copy();
                    leftover.stackSize -= inserted;
                    return leftover;
                }
            }
        }
    }

    public class ReadableItemHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            updateStacks();
            return topStack;
        }
    }

    public class ExtractionHandler extends BaseItemHandler {
        @Override
        public ItemStack getStackInSlot(int slot) {
            updateStacks();
            return bottomStack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            updateStacks();

            int amt = Math.min(amount, bottomStack != null ? bottomStack.stackSize : 0);
            if (amt > 0) {
                ItemStack stack = bottomStack.copy();
                stack.stackSize = amount;
                if (!simulate) {
                    bottomStack.stackSize -= amount;
                    sync();
                    cleanBarrel();
                    markChunkDirty();
                }
                return stack;
            } else {
                return null;
            }
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
        worldObj.markChunkDirty(pos, this);
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        item = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("item"));
        setItemCount(compound.getInteger("count"));
        orientation = Orientation.getOrientation(compound.getByte("dir"));
        woodLog = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("log"));
        woodSlab = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("slab"));
        type = Type.VALUES[compound.getByte("type")];
        if (woodLog == null) {
            woodLog = DEFAULT_LOG;
        }
        if (woodSlab == null) {
            woodSlab = DEFAULT_SLAB;
        }
        last_mentioned_count = getItemCount();
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        if (woodLog == null) {
            woodLog = DEFAULT_LOG;
        }
        if (woodSlab == null) {
            woodSlab = DEFAULT_SLAB;
        }

        ItemUtils.writeToNBT(item, compound, "item");
        ItemUtils.writeToNBT(woodLog, compound, "log");
        ItemUtils.writeToNBT(woodSlab, compound, "slab");
        compound.setByte("dir", (byte) orientation.ordinal());
        compound.setByte("type", (byte) type.ordinal());
        compound.setInteger("count", getItemCount());
        return compound;
    }

    private boolean scheduledTick = true;

    public int getLogicSpeed() {
        return 8;
    }

    @Override
    public void update() {
        if (getWorld() == null || getWorld().isRemote) {
            return;
        }

        if (!scheduledTick || (getWorld().getTotalWorldTime() % getLogicSpeed()) != 0) {
            return;
        }

        tick();
    }

    void tick() {
        if (!type.isHopping() || orientation == null) {
            return;
        }
        if (notice_target == this && worldObj.getStrongPower(pos) > 0) {
            return;
        }

        boolean youve_changed_jim = false;
        int itemCount = getItemCount();
        if (itemCount < getMaxSize()) {
            BlockPos upPos = getPos().offset(orientation.top);
            IItemHandler handler = CapabilityUtils.getCapability(getWorld(), upPos,
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation.top.getOpposite(),
                    true, true);

            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack got = handler.extractItem(i, 1, true);
                    if (got != null && taint(got)) {
                        got = handler.extractItem(i, 1, false);
                        taint(got);
                        changeItemCount(1);
                        updateStacks();
                        youve_changed_jim = true;
                    }
                }
            }
        }
        if (itemCount > 0) {
            BlockPos downPos = getPos().offset(orientation.top.getOpposite());
            IItemHandler handler = CapabilityUtils.getCapability(getWorld(), downPos,
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, orientation.top,
                    true, true);

            if (handler != null) {
                ItemStack bottom_item = getStackInSlot(1);
                if (bottom_item != null) {
                    ItemStack toPush = bottom_item.splitStack(1);
                    if (handler != null) {
                        boolean inserted = false;
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack got = handler.insertItem(i, toPush, false);
                            if (got == null) {
                                inserted = true;
                                updateStacks();
                                cleanBarrel();
                                youve_changed_jim = true;
                                break;
                            }
                        }
                        if (!inserted) {
                            bottom_item.stackSize++;
                        }
                    }
                }
            }
        }
        if (youve_changed_jim) {
            markDirty();
        }
    }

    private void needLogic() {
        scheduledTick = true;
    }

    public void neighborChanged() {
        if (type.isHopping()) {
            needLogic();
        }
    }

    public int getItemCount() {
        if (item == null) {
            return 0;
        }
        if (type == Type.CREATIVE) {
            return 32*item.getMaxStackSize();
        }
        if (topStack == null || !itemMatch(topStack)) {
            topStack = item.copy();
            topStack.stackSize = 0;
        }
        if (bottomStack == null || !itemMatch(bottomStack)) {
            bottomStack = item.copy();
            bottomStack.stackSize = 0;
        }
        int ret = bottomStack.stackSize + middleCount + topStack.stackSize;
        return ret;
    }

    public int getItemCountSticky() {
        int count = getItemCount();
        if (type == Type.STICKY) {
            count--;
            return Math.max(0, count);
        }
        return count;
    }

    public int getMaxSize() {
        int size = 64*64;
        if (item != null) {
            size = item.getMaxStackSize()*64;
        }
        if (type == Type.LARGER) {
            size *= 2;
        }
        return size;
    }

    public boolean itemMatch(ItemStack is) {
        if (is == null || item == null) {
            return false;
        }
        return ItemUtils.canMerge(item, is);
    }

    boolean taint(ItemStack is) {
        if (is == null && item == null) {
            return true;
        }
        if (is == null || isNested(is)) {
            return false;
        }
        if (item == null) {
            item = is.copy();
            item.stackSize = 0;
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

    public void setItemCount(int val) {
        topStack = bottomStack = null;
        middleCount = val;
        changeItemCount(0);
    }

    private boolean spammed = false;
    public void changeItemCount(int delta) {
        middleCount = getItemCount() + delta;
        if (middleCount < 0) {
            if (!spammed) {
                ModCharsetStorage.logger.error("Tried to set the item count to negative value " + middleCount + " at " + getPos());
                Thread.dumpStack();
                spammed = true;
            }
            middleCount = 0;
            item = null;
        }
        if (middleCount == 0) {
            topStack = bottomStack = item = null;
            updateClients(BarrelMessage.BarrelCount);
            markDirty();
            return;
        }
        if (middleCount > getMaxSize() && !spammed && worldObj != null) {
            ModCharsetStorage.logger.error("Factorization barrel size, " + middleCount + ", is larger than the maximum, " + getMaxSize() + ". Contents: " + item + " " + (item != null ? item.getItem() : "<null>") + " At: " + getPos() + " BarrelType = " + type);
            ModCharsetStorage.logger.error("Did the max stack size go down, or is someone doing something bad?");
            Thread.dumpStack();
            spammed = true;
        }
        if (topStack == null) {
            topStack = item.copy();
        }
        if (bottomStack == null) {
            bottomStack = item.copy();
        }
        topStack.stackSize = bottomStack.stackSize = 0;
        updateStacks();
        updateClients(BarrelMessage.BarrelCount);
        markDirty();
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
        orientation = SpaceUtil.getOrientation(getWorld(), getPos(), placer, face, hitX, hitY, hitZ);
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
            if (loadCount != 0) {
                ItemStack loadItem = getSilkedItem(is);
                if (loadItem != null) {
                    item = loadItem;
                    setItemCount(loadCount);
                }
            }
        }
    }

    public static ItemStack getSilkedItem(ItemStack is) {
        if (is == null || !is.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = is.getTagCompound();
        if (tag.hasKey("SilkItem")) {
            return ItemStack.loadItemStackFromNBT(is.getTagCompound().getCompoundTag("SilkItem"));
        }
        return null;
    }

    public static boolean isNested(ItemStack is) {
        return getSilkedItem(is) != null;
    }


    //Network stuff TODO

    void updateClients(BarrelMessage messageType) {
        if (hasWorldObj()) {
            markBlockForUpdate();
        }
    }

    /* FMLProxyPacket getPacket(BarrelMessage messageType) {
        if (messageType == BarrelMessage.BarrelItem) {
            return Core.network.TEmessagePacket(this, messageType, NetworkFactorization.nullItem(item), getItemCount());
        } else if (messageType == BarrelMessage.BarrelCount) {
            return Core.network.TEmessagePacket(this, messageType, getItemCount());
        } else {
            new IllegalArgumentException("bad MessageType: " + messageType).printStackTrace();
            return null;
        }
    }

    void updateClients(BarrelMessage messageType) {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        broadcastMessage(null, getPacket(messageType));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean handleMessageFromServer(Enum messageType, ByteBuf input) throws IOException {
        if (super.handleMessageFromServer(messageType, input)) {
            return true;
        }
        if (messageType == BarrelMessage.BarrelCount) {
            setItemCount(input.readInt());
            return true;
        }
        if (messageType == BarrelMessage.BarrelItem) {
            item = DataUtil.readStack(input);
            setItemCount(input.readInt());
            return true;
        }
        if (messageType == BarrelMessage.BarrelDoubleClickHack) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.playerController.currentItemHittingBlock = mc.thePlayer.getHeldItem();
            return true;
        }
        return false;
    } */

    void cleanBarrel() {
        if (getItemCount() == 0) {
            topStack = bottomStack = item = null;
            middleCount = 0;
        }
    }

    //Inventory code

    @Override
    public void markDirty() {
        super.markDirty();
        cleanBarrel();
        updateStacks();
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
                updateClients(BarrelMessage.BarrelItem);
            } else {
                updateClients(BarrelMessage.BarrelCount);
            }
            last_mentioned_count = c;
        }
    }

    private void updateStacks() {
        if (item == null) {
            topStack = bottomStack = null;
            middleCount = 0;
            return;
        }
        int count = getItemCount();
        if (count == 0) {
            topStack = bottomStack = null;
            middleCount = 0;
            return;
        }
        if (bottomStack == null) {
            bottomStack = item.copy();
            bottomStack.stackSize = 0;
        }
        if (type == Type.STICKY) {
            count--;
            if (count < 0) {
                return;
            }
        }
        int upperLine = getMaxSize() - item.getMaxStackSize();
        if (topStack == null) {
            topStack = item.copy();
        }
        if (count > upperLine) {
            topStack.stackSize = count - upperLine;
            count -= topStack.stackSize;
        } else {
            topStack.stackSize = 0;
        }
        bottomStack.stackSize = Math.min(item.getMaxStackSize(), count);
        count -= bottomStack.stackSize;
        middleCount = count;
        if (type == Type.STICKY) {
            middleCount++;
        }
    }

    public ItemStack getStackInSlot(int i) {
        updateStacks();
        if (i == 0) {
            return topStack;
        }
        if (i == 1) {
            return bottomStack;
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return isTop(facing) || isBottom(facing) || facing == null;
        } else {
            return false;
        }
    }

    private final IItemHandler[] handlers = new IItemHandler[] {
            new ExtractionHandler(),
            new InsertionHandler(),
            new ReadableItemHandler()
    };

    IItemHandler getItemHandler(int id) {
        return handlers[id];
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (isBottom(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers[0]);
            } else if (isTop(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers[1]);
            } else if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers[2]);
            }
        }

        return null;
    }

    public void clear() {
        setItemCount(0);
    }

    //Interaction

    long lastClick = -1000; //NOTE: This really should be player-specific!

    //*             Left-Click         Right-Click
    //* No Shift:   Remove stack       Add item
    //* Shift:      Remove 1 item      Use item
    //* Double:                        Add all but 1 item

    public boolean activate(EntityPlayer entityplayer, EnumFacing side, EnumHand hand) {
        // right click: put an item in
        if (worldObj.getTotalWorldTime() - lastClick < 10 && item != null) {
            addAllItems(entityplayer, hand);
            return true;
        }
        lastClick = worldObj.getTotalWorldTime();

        ItemStack held = entityplayer.getHeldItem(hand);
        if (held == null) {
            info(entityplayer);
            return true;
        }

        if (!worldObj.isRemote && isNested(held) && (item == null || itemMatch(held))) {
            new Notice(notice_target, "notice.charset.barrel.no").sendTo(entityplayer);
            return true;
        }

        NBTTagCompound tag = held.getTagCompound();
        if (tag != null && tag.hasKey("noFzBarrel")) {
            return false;
        }

        boolean veryNew = taint(held);

        if (!itemMatch(held)) {
            info(entityplayer);
            return true;
        }
        int free = getMaxSize() - getItemCount();
        if (free <= 0) {
            info(entityplayer);
            return true;
        }
        int take = Math.min(free, held.stackSize);
        held.stackSize -= take;
        changeItemCount(take);
        if (veryNew) {
            updateClients(BarrelMessage.BarrelItem);
        }
        if (held.stackSize == 0) {
            entityplayer.setHeldItem(hand, null);
        }
        return true;
    }

    void addAllItems(EntityPlayer entityplayer, EnumHand hand) {
        ItemStack held = entityplayer.getHeldItem(hand);
        if (held != null) {
            taint(held);
        }
        InventoryPlayer inv = entityplayer.inventory;
        int total_delta = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            int free_space = getMaxSize() - (getItemCount() + total_delta);
            if (free_space <= 0) {
                break;
            }
            ItemStack is = inv.getStackInSlot(i);
            if (is == null || is.stackSize <= 0) {
                continue;
            }
            if (!itemMatch(is)) {
                continue;
            }
            int toAdd = Math.min(is.stackSize, free_space);
            if (is == held && toAdd > 1) {
                toAdd -= 1;
            }
            total_delta += toAdd;
            is.stackSize -= toAdd;
            if (is.stackSize <= 0) {
                inv.setInventorySlotContents(i, null);
            }
        }
        changeItemCount(total_delta);
        if (total_delta > 0) {
            entityplayer.inventory.markDirty();
            // Core.proxy.updatePlayerInventory(entityplayer);
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
        int distance = PlayerUtils.getPuntStrengthInt(player);
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
            rotationAxis = SpaceUtil.rotate(dir, EnumFacing.UP);
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
                EnumFacing nTop = SpaceUtil.rotate(newOrientation.top, rotationAxis);
                EnumFacing nFace = SpaceUtil.rotate(newOrientation.facing, rotationAxis);
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
        if (is != null && is.isItemStackDamageable() && worldObj.rand.nextInt(4) == 0) {
            is.damageItem(distance, player);
            if (is.stackSize <= 0) {
                player.setHeldItem(hand, null);
            }
        }
        //spillItems(spillage); // Meh!
        return true;
    }

    public void click(EntityPlayer entityplayer) {
        EnumHand hand = EnumHand.MAIN_HAND;
        // left click: remove a stack, or punt if properly equipped
        if (punt(entityplayer, hand)) {
            return;
        }
        if (getItemCount() == 0 || item == null) {
            info(entityplayer);
            return;
        }

        ItemStack origHeldItem = entityplayer.getHeldItem(hand);
        if (ForgeHooks.canToolHarvestBlock(worldObj, pos, origHeldItem)) {
            return;
        }

        int to_remove = Math.min(item.getMaxStackSize(), getItemCount());
        if (entityplayer.isSneaking() && to_remove >= 1) {
            to_remove = 1;
        }
        if (to_remove > 1 && to_remove == getItemCount()) {
            to_remove--;
        }
        BlockPos dropPos = getPos();
        RayTraceResult result = RayTraceUtils.getCollision(getWorld(), getPos(), entityplayer, Block.FULL_BLOCK_AABB, 0);
        if (result != null && result.sideHit != null) {
            dropPos = dropPos.offset(result.sideHit);
        }
        ItemUtils.spawnItemEntity(worldObj, new Vec3d(dropPos).addVector(0.5, 0.5, 0.5), makeStack(to_remove), 0.2f, 0.2f, 0.2f, 1);
        Entity ent = null;
        // TODO
        //Entity ent = ItemUtil.giveItem(entityplayer, new Coord(this), makeStack(to_remove), SpaceUtil.getOrientation(last_hit_side));
        if (ent != null && ent.isDead && !(entityplayer instanceof FakePlayer)) {
            ItemStack newHeld = entityplayer.getHeldItem(hand);
            if (newHeld != origHeldItem) {
                // TODO
                // broadcastMessage(entityplayer, BarrelMessage.BarrelDoubleClickHack);
            }
        }
        changeItemCount(-to_remove);
        cleanBarrel();
    }

    void info(final EntityPlayer entityplayer) {
        new Notice(notice_target, new NoticeUpdater() {
            @Override
            public void update(Notice msg) {
                if (item == null && getItemCount() == 0) {
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
        if (item == null) {
            return null;
        }
        ItemStack ret = item.copy();
        ret.stackSize = count;
        assert ret.stackSize > 0 && ret.stackSize <= item.getMaxStackSize();
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
        if (item == null || getItemCount() <= 0 ) {
            return;
        }
        int count = getItemCount();
        for (int i = 0; i < maxStackDrop; i++) {
            int to_drop;
            to_drop = Math.min(item.getMaxStackSize(), count);
            count -= to_drop;
            ItemUtils.spawnItemEntity(worldObj, new Vec3d(getPos()).addVector(0.5, 0.5, 0.5), makeStack(to_drop), 0.2f, 0.2f, 0.2f, 1);
            // TODO
            //ItemUtil.giveItem(null, new Coord(this), makeStack(to_drop), null);
            if (count <= 0) {
                break;
            }
        }
        topStack = null;
        middleCount = 0;
        bottomStack = null;
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
            ItemStack stack1 = ItemStack.loadItemStackFromNBT(is.getTagCompound().getCompoundTag(name));
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
            getWorld().notifyNeighborsRespectDebug(pos, getBlockType());
        }
    }

    @Override
    public ItemStack getPickedBlock() {
        return getDroppedBlock();
    }

    public ItemStack getDroppedBlock() {
        ItemStack is = makeBarrel(type, woodLog, woodSlab);
        if (type == Type.SILKY && item != null && getItemCount() > 0 && broken_with_silk_touch) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                is.setTagCompound(tag);
            }
            tag.setInteger("SilkCount", getItemCount());
            NBTTagCompound si = new NBTTagCompound();
            item.writeToNBT(si);
            tag.setTag("SilkItem", si);
            tag.setLong("rnd", hashCode() + worldObj.getTotalWorldTime());
        }
        return is;
    }

    boolean broken_with_silk_touch = false;

    public boolean removedByPlayer(EntityPlayer player, boolean willHarvest) {
        if (cancelRemovedByPlayer(player)) return false;
        broken_with_silk_touch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getActiveItemStack()) > 0;
        return true;
    }

    private boolean cancelRemovedByPlayer(EntityPlayer player) {
        if (item == null) {
            return false;
        }
        if (player == null || !player.capabilities.isCreativeMode || player.isSneaking()) {
            return false;
        }
        if (player.worldObj.isRemote) {
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

    enum BarrelMessage {
        BarrelItem, BarrelCount, BarrelDoubleClickHack;
        static final BarrelMessage[] VALUES = values();
    }

    // TODO
    // @Override
    public Enum[] getMessages() {
        return BarrelMessage.VALUES;
    }
}
