package pl.asie.charset.api.locks;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public interface ILockingEntity {
    boolean isLocked();
    boolean isLockValid(@Nullable TileEntity tile);
    int getLockEntityId();
    String getLockKey();
}
