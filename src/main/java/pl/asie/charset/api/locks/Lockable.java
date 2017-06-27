package pl.asie.charset.api.locks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public final class Lockable implements INBTSerializable<NBTTagCompound> {
    private final World world;
    private ILockingEntity lock;

    public Lockable() {
        this(null);
    }

    public Lockable(World world) {
        this.world = world;
    }

    public ILockingEntity getLock() {
        if (lock != null && !lock.isLockValid()) {
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
            compound.setInteger("lockILockingEntityId", lock.getLockEntityId());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("lockILockingEntityId") && world != null) {
            lock = (ILockingEntity) world.getEntityByID(nbt.getInteger("lockILockingEntityId"));
        } else {
            lock = null;
        }
    }
}
