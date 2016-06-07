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

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.lib.factorization.FzOrientation;
import pl.asie.charset.lib.factorization.SpaceUtil;
import pl.asie.charset.storage.ModCharsetStorage;

import javax.annotation.Nullable;

public class EntityMinecartDayBarrel extends EntityMinecart {
    private static final DataParameter<Optional<ItemStack>> BARREL_ITEM = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.OPTIONAL_ITEM_STACK);
    private static final DataParameter<Optional<ItemStack>> BARREL_LOG = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.OPTIONAL_ITEM_STACK);
    private static final DataParameter<Optional<ItemStack>> BARREL_SLAB = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.OPTIONAL_ITEM_STACK);
    private static final DataParameter<Byte> BARREL_ORIENTATION = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BARREL_TYPE = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> BARREL_ITEM_COUNT = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.VARINT);

    protected TileEntityDayBarrel barrel;
    private IItemHandler itemHandler = new MinecartItemHandler();
    private int activatorRailTicks = 0;

    public class MinecartItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return barrel.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate && slot == 0) {
                updateDataWatcher(false);
            }
            return slot == 0 ? barrel.getItemHandler(1).insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!simulate && slot == 1) {
                updateDataWatcher(false);
            }
            return slot == 1 ? barrel.getItemHandler(0).extractItem(slot, amount, simulate) : null;
        }
    }

    public EntityMinecartDayBarrel(World world) {
        super(world);
    }

    public EntityMinecartDayBarrel(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public void killMinecart(DamageSource damage) {
        this.setDead();
        if (barrel != null) {
            barrel.dropContents();
        }

        ItemStack itemstack = getCartItem();
        if (this.getCustomNameTag() != null) {
            itemstack.setStackDisplayName(getCustomNameTag());
        }

        this.entityDropItem(itemstack, 0.0F);
    }

    @Override
    public ItemStack getCartItem() {
        ItemStack stack = barrel.getDroppedBlock();
        ItemStack realStack = new ItemStack(ModCharsetStorage.barrelCartItem, 1);
        realStack.setTagCompound(stack.getTagCompound());
        return realStack;
    }

    @Override
    public EntityMinecart.Type getType() {
        return EntityMinecart.Type.CHEST;
    }

    public void initFromStack(ItemStack is) {
        barrel.loadFromStack(is);
        updateDataWatcher(true);
    }

    private void createBarrel() {
        if (barrel != null) return;
        barrel = new TileEntityDayBarrel();
        barrel.setWorldObj(worldObj);
        barrel.setPos(BlockPos.ORIGIN);
        barrel.validate();
        barrel.orientation = FzOrientation.fromDirection(EnumFacing.WEST).pointTopTo(EnumFacing.UP);
        barrel.notice_target = this;
    }

    @Override
    protected final void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        barrel.readNBTData(tag, false);
        updateDataWatcher(true);
    }

    @Override
    protected final void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        barrel.writeNBTData(tag, false);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        createBarrel();

        dataManager.register(BARREL_ITEM, Optional.fromNullable(barrel.item));
        dataManager.register(BARREL_ITEM_COUNT, barrel.getItemCount());
        dataManager.register(BARREL_LOG, Optional.fromNullable(barrel.woodLog));
        dataManager.register(BARREL_SLAB, Optional.fromNullable(barrel.woodSlab));
        dataManager.register(BARREL_ORIENTATION, (byte) barrel.orientation.ordinal());
        dataManager.register(BARREL_TYPE, (byte) barrel.type.ordinal());
    }

    private void updateDataWatcher(boolean full) {
        if (!worldObj.isRemote) {
            dataManager.set(BARREL_ITEM, Optional.fromNullable(barrel.item));
            dataManager.set(BARREL_ITEM_COUNT, barrel.getItemCount());
            if (full) {
                dataManager.set(BARREL_LOG, Optional.fromNullable(barrel.woodLog));
                dataManager.set(BARREL_SLAB, Optional.fromNullable(barrel.woodSlab));
                dataManager.set(BARREL_ORIENTATION, (byte) barrel.orientation.ordinal());
                dataManager.set(BARREL_TYPE, (byte) barrel.type.ordinal());
            }
        }
    }

    @Override
    public void onActivatorRailPass(int x, int y, int z, boolean powered) {
        if (powered) {
            activatorRailTicks = barrel.getLogicSpeed();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (worldObj.isRemote && dataManager.isDirty()) {
            barrel.item = dataManager.get(BARREL_ITEM).orNull();
            barrel.setItemCount(dataManager.get(BARREL_ITEM_COUNT));
            barrel.woodLog = dataManager.get(BARREL_LOG).or(TileEntityDayBarrel.DEFAULT_LOG);
            barrel.woodSlab = dataManager.get(BARREL_SLAB).or(TileEntityDayBarrel.DEFAULT_SLAB);
            barrel.orientation = FzOrientation.getOrientation(dataManager.get(BARREL_ORIENTATION));
            barrel.type = TileEntityDayBarrel.Type.values()[dataManager.get(BARREL_TYPE)];
        }

        if (!worldObj.isRemote) {
            barrel.setPos(new BlockPos(this));
            if (activatorRailTicks > 0) activatorRailTicks--;

            if (activatorRailTicks <= 0 && worldObj.getTotalWorldTime() % barrel.getLogicSpeed() == 0) {
                barrel.tick();
                updateDataWatcher(false);
            }
        }
    }

    @Override
    public IBlockState getDisplayTile() {
        return ((IExtendedBlockState) ModCharsetStorage.barrelBlock.getDefaultState()).withProperty(BlockBarrel.BARREL_INFO, BarrelCacheInfo.from(barrel));
    }

    @Override
    public IBlockState getDefaultDisplayTile() {
        return ModCharsetStorage.barrelBlock.getDefaultState();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float f) {
        if (source.getEntity() instanceof EntityPlayer) {
            int oldItemCount = barrel.getItemCount();
            barrel.click((EntityPlayer) source.getEntity()); // TODO
            updateDataWatcher(false);
            if (source.getEntity().isSneaking()) {
                return super.attackEntityFrom(source, f);
            }
            if (barrel.type == TileEntityDayBarrel.Type.CREATIVE) {
                return false;
            }
            if (barrel.getItemCount() != oldItemCount) {
                return false;
            }
        }
        return super.attackEntityFrom(source, f);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.minecart.MinecartInteractEvent(this, player, stack, hand))) {
            return true;
        }

        boolean result = barrel.activate(player, null, hand);
        updateDataWatcher(false);
        return result;
    }

    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler)
                : null;
    }
}
