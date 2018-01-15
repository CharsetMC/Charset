/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.api.locks;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import pl.asie.charset.api.lib.ICacheable;

public final class Lockable implements ICacheable, INBTSerializable<NBTTagCompound> {
    private final TileEntity owner;
    private ILockingEntity lock;

    public Lockable() {
        this(null);
    }

    public Lockable(TileEntity owner) {
        this.owner = owner;
    }

    public ILockingEntity getLock() {
        if (lock != null && !lock.isLockValid(null)) {
            lock = null;
        }
        return lock;
    }

    public boolean hasLock() {
        return getLock() != null;
    }

    public boolean addLock(ILockingEntity lock) {
        if (this.getLock() == null) {
            this.lock = lock;
            return true;
        } else {
            return false;
        }
    }
    public boolean removeLock(ILockingEntity lock) {
        if (hasLock() && this.getLock() == lock) {
            this.lock = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        if (hasLock()) {
            compound.setInteger("lockId", lock.getLockEntityId());
            // compound.setString("lockKey", lock.getLockKey());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        lock = null;

        if (nbt.hasKey("lockId", Constants.NBT.TAG_ANY_NUMERIC) && owner.getWorld() != null) {
            Entity entity = owner.getWorld().getEntityByID(nbt.getInteger("lockId"));
            if (entity instanceof ILockingEntity) {
                lock = (ILockingEntity) entity;
            }
        } else if (nbt.hasKey("lockILockingEntityId", Constants.NBT.TAG_ANY_NUMERIC) && owner.getWorld() != null /* derp */) {
            Entity entity = owner.getWorld().getEntityByID(nbt.getInteger("lockILockingEntityId"));
            if (entity instanceof ILockingEntity) {
                lock = (ILockingEntity) entity;
            }
        }
    }

    @Override
    public boolean isCacheValid() {
        return !owner.isInvalid();
    }
}
