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

package pl.asie.simplelogic.wires.logic;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.simplelogic.wires.LogicWireUtils;

public class LogicWireProvider extends WireProvider {
    public final WireType type;
    public final int color;

    public LogicWireProvider(WireType type, int color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public boolean canProvidePower() {
        return type != WireType.BUNDLED;
    }

    @Override
    public Wire create(IWireContainer container, WireFace location) {
        PartWireSignalBase wire = null;
        switch (type) {
            case NORMAL:
                wire = new PartWireNormal(container, this, location);
                break;
            case INSULATED:
                wire = new PartWireInsulated(container, this, location);
                wire.setColor(color);
                break;
            case BUNDLED:
                wire = new PartWireBundled(container, this, location);
                break;
        }

        return wire;
    }

    @Override
    public boolean canPlace(IBlockAccess access, BlockPos pos, WireFace face) {
        return face == WireFace.CENTER || LogicWireUtils.canPlaceWire(access, pos.offset(face.facing), face.facing != null ? face.facing.getOpposite() : null);
    }

    @Override
    public float getWidth() {
        return LogicWireUtils.width(type) / 16.0F;
    }

    @Override
    public float getHeight() {
        return LogicWireUtils.height(type) / 16.0F;
    }

    @Override
    public ResourceLocation getTexturePrefix() {
        return new ResourceLocation("simplelogic:blocks/wire/wire_" + type.name().toLowerCase());
    }
}
