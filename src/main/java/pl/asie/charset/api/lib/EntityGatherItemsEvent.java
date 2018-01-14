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

package pl.asie.charset.api.lib;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collection;

/**
 * This is an event used by Charset to gather the ItemStacks an Entity is
 * wearing (for example, armor slots or Baubles slots), as well as ItemStacks
 * an Entity is holding (for example, main hand and off hand slots).
 */
public class EntityGatherItemsEvent extends Event {
    private final boolean collectHeld, collectWorn;
    private final Entity entity;
    private final Collection<ItemStack> stacks;

    public EntityGatherItemsEvent(Entity entity, Collection<ItemStack> stacks, boolean collectHeld, boolean collectWorn) {
        this.entity = entity;
        this.stacks = stacks;
        this.collectHeld = collectHeld;
        this.collectWorn = collectWorn;
    }

    public Entity getEntity() {
        return entity;
    }

    public void addStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }

    public boolean collectsHeld() {
        return collectHeld;
    }

    public boolean collectsWorn() {
        return collectWorn;
    }
}
