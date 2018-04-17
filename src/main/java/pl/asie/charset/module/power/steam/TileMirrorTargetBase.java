package pl.asie.charset.module.power.steam;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.module.power.steam.api.IMirror;
import pl.asie.charset.module.power.steam.api.IMirrorTarget;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class TileMirrorTargetBase extends TileBase implements IMirrorTarget {
    protected final Set<IMirror> mirrors = new HashSet<>();

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CharsetPowerSteam.MIRROR_TARGET) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CharsetPowerSteam.MIRROR_TARGET) {
            return CharsetPowerSteam.MIRROR_TARGET.cast(this);
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidate(InvalidationType type) {
        super.invalidate(type);
        if (type == InvalidationType.REMOVAL) {
            mirrors.forEach(IMirror::requestMirrorTargetRefresh);
        }
    }

    @Override
    public void registerMirror(IMirror mirror) {
        mirrors.add(mirror);
    }

    @Override
    public void unregisterMirror(IMirror mirror) {
        mirrors.remove(mirror);
    }
}
