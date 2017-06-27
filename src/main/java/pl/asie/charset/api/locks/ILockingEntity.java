package pl.asie.charset.api.locks;

public interface ILockingEntity {
    boolean isLocked();
    boolean isLockValid();
    int getLockEntityId();
    String getLockKey();
}
