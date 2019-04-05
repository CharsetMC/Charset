/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.wires;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWireContainer {
    World world();
    BlockPos pos();
    void requestNeighborUpdate(int connectionMask);
    default void requestNeighborUpdate(EnumFacing facing) {
        requestNeighborUpdate(1 << (facing.ordinal() + 8));
    }
    void requestNetworkUpdate();
    void requestRenderUpdate();
    void dropWire();

    class Dummy implements IWireContainer {
        @Override
        public World world() {
            return null;
        }

        @Override
        public BlockPos pos() {
            return null;
        }

        @Override
        public void requestNeighborUpdate(int connectionMask) {

        }

        @Override
        public void requestNetworkUpdate() {

        }

        @Override
        public void requestRenderUpdate() {

        }

        @Override
        public void dropWire() {

        }
    }
}
