/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.lib.IAxisRotatable;

import java.util.List;
import java.util.function.Function;

public class AxisRotatableCombiner implements Function<List<IAxisRotatable>, IAxisRotatable> {
    @Override
    public IAxisRotatable apply(List<IAxisRotatable> iAxisRotatableList) {
        return (axis, simulate) -> {
            for (IAxisRotatable rotatable : iAxisRotatableList) {
                if (!rotatable.rotateAround(axis, true)) {
                    return false;
                }
            }

            if (!simulate) {
                for (IAxisRotatable rotatable : iAxisRotatableList) {
                    rotatable.rotateAround(axis, false);
                }
            }

            return true;
        };
    }
}
