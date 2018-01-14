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

package pl.asie.charset.api.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class ActionConsumer implements INBTSerializable<NBTTagCompound> {
    protected final World world;
    private final IActionListener listener;
    private long lastConsumedTime;

    public ActionConsumer(World world) {
        this.world = world;
        this.listener = this instanceof IActionListener ? (IActionListener) this : null;
    }

    public ActionConsumer(World world, IActionListener listener) {
        this.world = world;
        this.listener = listener;
    }

    final void receiveAction() {
        lastConsumedTime = world.getTotalWorldTime();
        if (listener != null) {
            listener.onActionReceived();
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("lct", lastConsumedTime);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        if (compound.hasKey("lct")) {
            lastConsumedTime = compound.getLong("lct");
        } else {
            lastConsumedTime = 0;
        }
    }
}
