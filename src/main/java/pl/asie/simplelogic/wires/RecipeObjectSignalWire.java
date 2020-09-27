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

package pl.asie.simplelogic.wires;

/**
 * Created by asie on 6/13/16.
 */
public class RecipeObjectSignalWire /* implements IRecipeObject */ {
   /* private final WireType type;
    private final boolean freestanding;

    public RecipeObjectSignalWire(WireType type, boolean freestanding) {
        this.type = type;
        this.freestanding = freestanding;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack != null && stack.getItem() == WireManager.ITEM) {
            WireFactory factory = WireManager.ITEM.getFactory(stack);
            if (factory instanceof WireSignalFactory) {
                WireSignalFactory signalFactory = (WireSignalFactory) factory;
                if (type == null || signalFactory.type == type) {
                    boolean targetFreestanding = WireManager.ITEM.isFreestanding(stack);
                    return freestanding ? targetFreestanding : !targetFreestanding;
                }
            }
        }

        return false;
    }

    @Override
    public Object preview() {
        List<ItemStack> stacks = new ArrayList<>();
        for (WireFactory f : SimpleLogicWires.wireFactories) {
            if (((WireSignalFactory) f).type == type) {
                stacks.add(WireManager.ITEM.getStack(f, freestanding));
            }
        }
        return stacks;
    } */
}
