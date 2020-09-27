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

package pl.asie.charset.module.storage.barrels;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.Orientation;

public class EntityMinecartDayBarrel extends EntityMinecart {
    private static final DataParameter<ItemStack> BARREL_ITEM = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<String> BARREL_LOG = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.STRING);
    private static final DataParameter<String> BARREL_SLAB = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.STRING);
    private static final DataParameter<Byte> BARREL_ORIENTATION = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> BARREL_UPGRADES = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> BARREL_ITEM_COUNT = EntityDataManager.createKey(EntityMinecartDayBarrel.class, DataSerializers.VARINT);

    protected TileEntityDayBarrel barrel;
    private IItemHandler itemHandler = new MinecartItemHandler();
    private int activatorRailTicks = 0;

    public class MinecartItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return barrel.readOnlyView.getSlotLimit(slot);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return barrel.readOnlyView.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack result = barrel.insertionView.insertItem(slot, stack, simulate);
            if (!simulate) {
                updateDataWatcher(false);
            }
            return result;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack result = barrel.extractionView.extractItem(slot, amount, simulate);
            if (!simulate) {
                updateDataWatcher(false);
            }
            return result;
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

        ItemStack itemstack = getCartItem();
        if (this.hasCustomName()) {
            itemstack.setStackDisplayName(getCustomNameTag());
        }

        this.entityDropItem(itemstack, 0.0F);
        for (ItemStack stack : barrel.getContentDrops(false)) {
            this.entityDropItem(stack, 0.0F);
        }
    }

    @Override
    public ItemStack getCartItem() {
        ItemStack stack = barrel.getDroppedBlock(CharsetStorageBarrels.barrelBlock.getDefaultState());
        ItemStack realStack = new ItemStack(CharsetStorageBarrels.barrelCartItem, 1);
        realStack.setTagCompound(stack.getTagCompound());
        return realStack;
    }

    @Override
    public EntityMinecart.Type getType() {
        return EntityMinecart.Type.CHEST;
    }

    public EntityMinecartDayBarrel initFromStack(ItemStack is) {
        barrel.loadFromStack(is);
        updateDataWatcher(true);
        return this;
    }

    public EntityMinecartDayBarrel initFromTile(TileEntityDayBarrel barrel) {
        this.barrel = barrel;
        barrel.setWorld(world);
        barrel.setPos(BlockPos.ORIGIN);
        barrel.isEntity = true;
        barrel.orientation = Orientation.fromDirection(EnumFacing.WEST).pointTopTo(EnumFacing.UP);
        barrel.notice_target = this;
        barrel.validate();
        updateDataWatcher(true);
        return this;
    }

    public TileEntityDayBarrel getTileInternal() {
        return barrel;
    }

    private void createBarrel() {
        if (barrel != null) return;
        barrel = new TileEntityDayBarrel();
        barrel.setWorld(world);
        barrel.setPos(BlockPos.ORIGIN);
        barrel.isEntity = true;
        barrel.orientation = Orientation.fromDirection(EnumFacing.WEST).pointTopTo(EnumFacing.UP);
        barrel.notice_target = this;
        barrel.validate();
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

    protected int serializeUpgrades() {
        int i = 0;
        for (BarrelUpgrade u : barrel.upgrades)
            i |= (1 << u.ordinal());
        return i;
    }

    protected void deserializeUpgrades(int i) {
        barrel.upgrades.clear();
        int j = 0;
        while (i > 0) {
            if ((i & 1) != 0) {
                barrel.upgrades.add(BarrelUpgrade.values()[j]);
            }
            j++;
            i >>= 1;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        createBarrel();

        dataManager.register(BARREL_ITEM, barrel.getItemSafe());
        dataManager.register(BARREL_ITEM_COUNT, barrel.getItemCount());
        dataManager.register(BARREL_LOG, barrel.woodLog.getId());
        dataManager.register(BARREL_SLAB, barrel.woodSlab.getId());
        dataManager.register(BARREL_ORIENTATION, (byte) barrel.orientation.ordinal());
        dataManager.register(BARREL_UPGRADES, serializeUpgrades());
    }

    private void updateDataWatcher(boolean full) {
        if (!world.isRemote) {
            dataManager.set(BARREL_ITEM, barrel.getItemSafe());
            dataManager.set(BARREL_ITEM_COUNT, barrel.getItemCount());
            if (full) {
                dataManager.set(BARREL_LOG, barrel.woodLog.getId());
                dataManager.set(BARREL_SLAB, barrel.woodSlab.getId());
                dataManager.set(BARREL_ORIENTATION, (byte) barrel.orientation.ordinal());
                dataManager.set(BARREL_UPGRADES, serializeUpgrades());
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

        if (world.isRemote && dataManager.isDirty()) {
            ItemStack item = dataManager.get(BARREL_ITEM);
            item.setCount(dataManager.get(BARREL_ITEM_COUNT));
            barrel.setItemUnsafe(item);
            barrel.woodLog = ItemMaterialRegistry.INSTANCE.getMaterial(dataManager.get(BARREL_LOG));
            barrel.woodSlab = ItemMaterialRegistry.INSTANCE.getMaterial(dataManager.get(BARREL_SLAB));
            barrel.orientation = Orientation.getOrientation(dataManager.get(BARREL_ORIENTATION));
            deserializeUpgrades(dataManager.get(BARREL_UPGRADES));
        }

        if (!world.isRemote) {
            barrel.setPos(new BlockPos(this));
            if (activatorRailTicks > 0) activatorRailTicks--;

            if (activatorRailTicks <= 0 && world.getTotalWorldTime() % barrel.getLogicSpeed() == 0) {
                barrel.tick();
                updateDataWatcher(false);
            }
        }
    }

    @Override
    public IBlockState getDisplayTile() {
        return ((IExtendedBlockState) CharsetStorageBarrels.barrelBlock.getDefaultState()).withProperty(BlockBarrel.BARREL_INFO, BarrelCacheInfo.from(barrel));
    }

    @Override
    public IBlockState getDefaultDisplayTile() {
        return CharsetStorageBarrels.barrelBlock.getDefaultState();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float f) {
        // TODO: Fun uses of getTrueSource?
        if (source.getImmediateSource() instanceof EntityPlayer) {
            int oldItemCount = barrel.getItemCount();
            if (!world.isRemote) {
                barrel.click((EntityPlayer) source.getImmediateSource()); // TODO
                updateDataWatcher(false);
            }
            if (source.getImmediateSource().isSneaking()) {
                return super.attackEntityFrom(source, f);
            }
            if (world.isRemote) {
                return true;
            }
            if (barrel.upgrades.contains(BarrelUpgrade.INFINITE)) {
                return false;
            }
            if (barrel.getItemCount() != oldItemCount) {
                return false;
            }
        }
        return super.attackEntityFrom(source, f);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.minecart.MinecartInteractEvent(this, player, hand)))
            return true;
        return super.processInitialInteract(player, hand);
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (!world.isRemote) {
            boolean result = barrel.activate(player, null, hand);
            updateDataWatcher(false);
            return result ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public int getComparatorLevel() {
        return barrel.getComparatorValue(15);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability == Capabilities.BARREL
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        } else if (capability == Capabilities.BARREL) {
            return Capabilities.BARREL.cast(barrel);
        } else {
            return super.getCapability(capability, facing);
        }
    }
}
