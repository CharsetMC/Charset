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

import com.google.common.base.Predicate;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import pl.asie.charset.api.locks.ILockingEntity;
import pl.asie.charset.api.locks.Lockable;
import pl.asie.charset.lib.capability.Capabilities;

public class EntityLock extends EntityHanging implements IEntityAdditionalSpawnData, ILockingEntity {
    static final DataParameter<Integer> COLOR_0 = EntityDataManager.createKey(EntityLock.class, DataSerializers.VARINT);
    static final DataParameter<Integer> COLOR_1 = EntityDataManager.createKey(EntityLock.class, DataSerializers.VARINT);

    private static final DataParameter<EnumFacing> HANGING_ROTATION = EntityDataManager.createKey(EntityLock.class, DataSerializers.FACING);
    private static final Predicate<Entity> IS_HANGING_ENTITY = new Predicate<Entity>() {
        public boolean apply(Entity entity) {
            return entity instanceof EntityHanging;
        }
    };

    private String lockKey = null;
    protected int color = -1;
    private TileEntity tileCached;
    private boolean locked = true;

    public EntityLock(World worldIn) {
        super(worldIn);
    }

    public EntityLock(World worldIn, ItemStack stack, BlockPos pos, EnumFacing facing) {
        super(worldIn, pos);
        this.setColors(stack.getTagCompound());
        this.setLockKey(((ItemLock) stack.getItem()).getRawKey(stack));
        this.updateFacingWithBoundingBox(facing);
    }

    private void setColors(NBTTagCompound compound) {
        if (compound != null) {
            color = compound.hasKey("color") ? compound.getInteger("color") : -1;
        } else {
            color = -1;
        }
    }

    private void setLockKey(String s) {
        this.lockKey = s;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setLockKey(compound.hasKey("key") ? compound.getString("key") : null);
        setColors(compound);
    }

    @Override
    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        } else {
            return I18n.translateToLocal("item.charset.lock.name");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (lockKey != null) {
            compound.setString("key", lockKey);
        }
        if (color != -1) {
            compound.setInteger("color1", color);
        }
    }

    public Lockable getAttachedLock() {
        TileEntity tile = getAttachedTile();
        if (tile != null && tile.hasCapability(Capabilities.LOCKABLE, null)) {
            Lockable lock = tile.getCapability(Capabilities.LOCKABLE, null);
            if (!lock.hasLock()) {
                if (LockEventHandler.getLock(tile) == null) {
                    lock.addLock(this);
                }
            }

            return lock.getLock() == this ? lock : null;
        } else {
            return null;
        }
    }

    public BlockPos getAttachmentPos() {
        return this.hangingPosition.offset(this.facingDirection.getOpposite());
    }

    public TileEntity getAttachedTile() {
        if (tileCached == null || tileCached.isInvalid()) {
            BlockPos pos = getAttachmentPos();
            tileCached = world.getTileEntity(pos);
        }

        return tileCached;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setEntityInvulnerable(true);
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayer && entityIn.isSneaking()) {
            if (!this.world.isRemote) {
                if (LockEventHandler.unlockOrRaiseError((EntityPlayer) entityIn, getAttachedTile(), getAttachedLock())) {
                    if (!this.isDead) {
                        this.setDead();
                        this.onBroken(entityIn);
                    }
                }

                return true;
            } else {
                return super.hitByEntity(entityIn);
            }
        }

        return super.hitByEntity(entityIn);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // TODO: Remove in 1.13
        if (!world.isRemote && getAttachedTile() instanceof ILockableContainer) {
            ILockableContainer container = (ILockableContainer) tileCached;
            if (container.isLocked() && container.getLockCode().getLock().startsWith("charset")) {
                container.setLockCode(LockCode.EMPTY_CODE);
            }
        }

        if (!world.isRemote && lockKey != null && getAttachedLock() == null) {
            drop();
        }
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (!world.isRemote && hand == EnumHand.MAIN_HAND && lockKey != null && getAttachedLock() != null) {
            boolean canUnlock = LockEventHandler.unlockOrRaiseError(player, getAttachedTile(), getAttachedLock());

            if (canUnlock) {
                Lockable lock = getAttachedLock();
                if (lock != null) {
                    locked = false;

                    BlockPos pos = this.hangingPosition.offset(this.facingDirection.getOpposite());
                    IBlockState state = world.getBlockState(pos);

                    state.getBlock().onBlockActivated(world, pos, state, player, hand, this.facingDirection,
                            0.5F + this.facingDirection.getXOffset() * 0.5F,
                            0.5F + this.facingDirection.getYOffset() * 0.5F,
                            0.5F + this.facingDirection.getZOffset() * 0.5F
                    );

                    locked = true;
                }

                return EnumActionResult.SUCCESS;
            } else {
                return EnumActionResult.FAIL;
            }
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public float getCollisionBorderSize()
    {
        return 0.0F;
    }

    @Override
    public boolean onValidSurface() {
        if (getAttachedLock() == null) {
            return false;
        }

        if (!this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()) {
            return false;
        } else {
            return this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), IS_HANGING_ENTITY).isEmpty();
        }
    }

    @Override
    public int getWidthPixels() {
        return 8;
    }

    @Override
    public int getHeightPixels() {
        return 8;
    }

    private ItemStack createItemStack(Item item) {
        ItemStack lock = new ItemStack(item);
        lock.setTagCompound(new NBTTagCompound());
        if (lockKey != null) {
            lock.getTagCompound().setString("key", lockKey);
        }
        if (color != -1) {
            lock.getTagCompound().setInteger("color", color);
        }
        return lock;
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return createItemStack(CharsetStorageLocks.keyItem);
    }

    public void drop() {
        Lockable lock = getAttachedLock();
        if (lock != null) {
            lock.removeLock(this);
        }
        this.entityDropItem(createItemStack(CharsetStorageLocks.lockItem), 0.0F);
        this.setDead();
    }

    @Override
    public void onBroken(Entity brokenEntity) {
        world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        drop();
    }

    @Override
    public void playPlaceSound() {
        world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_HIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeByte(facingDirection.ordinal());
        buffer.writeInt(color);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.updateFacingWithBoundingBox(EnumFacing.byIndex(buffer.readUnsignedByte()));
        color = buffer.readInt();
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean isLockValid(TileEntity tile) {
        if (!isEntityAlive()) {
            return false;
        }

        if (tile == null) return true;
        if (getAttachedTile().getPos().equals(tile.getPos())) return true;

        return false;
    }

    @Override
    public int getLockEntityId() {
        return getEntityId();
    }

    @Override
    public String getLockKey() {
        return lockKey;
    }
}
