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
