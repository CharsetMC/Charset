/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.pipes.pipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.FluidHandlerHelper;
import pl.asie.charset.pipes.CharsetPipes;
import pl.asie.charset.pipes.PipeUtils;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class PipeFluidContainer implements ITickable {
    private static boolean CAN_BE_DRAINED = false;

    public class Properties implements IFluidTankProperties {
        private final FluidStack stack;
        private final int capacity;

        public Properties(FluidStack stack, int amount, int capacity) {
            if (stack != null) {
                this.stack = stack.copy();
                this.stack.amount = amount;
            } else {
                this.stack = null;
            }
            this.capacity = capacity;
        }

        @Nullable
        @Override
        public FluidStack getContents() {
            return stack;
        }

        @Override
        public int getCapacity() {
            return capacity;
        }

        @Override
        public boolean canFill() {
            return stack == null || stack.amount < capacity;
        }

        @Override
        public boolean canDrain() {
            return CAN_BE_DRAINED && stack != null && stack.amount > 0;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return stack == null || (stack.amount < capacity && stack.isFluidEqual(fluidStack));
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return CAN_BE_DRAINED && stack != null && stack.amount > 0 && stack.isFluidEqual(fluidStack);
        }
    }

    public enum TankType {
	    NONE,
	    INPUT,
	    OUTPUT;

	    private static final TankType[] INVERTED = {NONE, OUTPUT, INPUT};

	    public TankType invert() {
		    return INVERTED[ordinal()];
	    }
    }

    public class Tank implements IFluidHandler {
        public final EnumFacing location;
	    public TankType type = TankType.NONE;
        public int amount;
        private boolean dirty;

        public Tank(EnumFacing location) {
            this.location = location;
        }

        public FluidStack getType() {
            return fluidStack;
        }

        public int get() {
            return amount;
        }

        public boolean isDirty() {
            return dirty;
        }

        public boolean removeDirty() {
            if (dirty) {
                dirty = false;
                return true;
            } else {
                return false;
            }
        }

        public int add(int amount, boolean simulate) {
            int targetAmount = Math.min(amount, TANK_SIZE - this.amount);
            if (!simulate && targetAmount > 0) {
                this.amount += targetAmount;
                dirty = true;
            }
            return targetAmount;
        }

        public int remove(int amount, boolean simulate) {
            int targetAmount = Math.min(this.amount, amount);
            if (!simulate && targetAmount > 0) {
                this.amount -= targetAmount;
                dirty = true;
                onRemoval();
            }
            return targetAmount;
        }

        public FluidTankInfo getInfo() {
            FluidStack stack = getType().copy();
            stack.amount = amount;
            return new FluidTankInfo(stack, getCapacity());
        }

        public int getCapacity() {
            return TANK_SIZE;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[]{new Properties(fluidStack, amount, TANK_SIZE)};
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount == 0) {
                return 0;
            }

            if (fluidStack == null) {
                if (doFill) {
                    fluidStack = resource.copy();
                    fluidDirty = true;
                }
            } else if (!fluidStack.isFluidEqual(resource)) {
                return 0;
            }

            return add(resource.amount, !doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount == 0) {
                return null;
            }

            if (CAN_BE_DRAINED) {
                return fluidStack != null && resource.isFluidEqual(fluidStack) ? drain(resource.amount, doDrain) : null;
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (fluidStack == null) {
                return null;
            }

            if (CAN_BE_DRAINED && maxDrain > 0) {
                FluidStack stack = fluidStack.copy();
                int r = remove(maxDrain, !doDrain);
                if (r <= 0) {
                    return null;
                } else {
                    stack.amount = r;
                    return stack;
                }
            } else {
                return null;
            }
        }

	    public int getFreeSpace() {
		    return TANK_SIZE - amount;
	    }
    }

    public static final int TANK_RATE = 80;
    final Tank[] tanks = new Tank[7];
    FluidStack fluidStack;
    int fluidColor;
    boolean fluidDirty;

    private static final int TANK_SIZE = 250;
    private final TilePipe owner;

    public PipeFluidContainer(TilePipe owner) {
        this.owner = owner;
        for (int i = 0; i < 7; i++) {
            tanks[i] = new Tank(i < 6 ? EnumFacing.getFront(i) : null);
        }
    }

    public void onRemoval() {
        int total = 0;
        for (Tank t : tanks) {
            total += t.amount;
        }
        if (total == 0) {
            fluidStack = null;
            fluidDirty = true;
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        if (fluidStack != null) {
            fluidStack.writeToNBT(nbt);
            int[] amt = new int[7];
            byte[] flags = new byte[7];
            for (int i = 0; i <= 6; i++) {
                amt[i] = tanks[i].amount;
                flags[i] = (byte) tanks[i].type.ordinal();
            }
            nbt.setIntArray("TankAmts", amt);
            nbt.setByteArray("TankFlags", flags);
        }
    }

    public void readFromNBT(NBTTagCompound nbt) {
        fluidStack = FluidStack.loadFluidStackFromNBT(nbt);
        fluidDirty = true;
        for (int i = 0; i <= 6; i++) {
            tanks[i].amount = 0;
            tanks[i].dirty = true;
        }

        if (fluidStack != null) {
            int[] amt = nbt.getIntArray("TankAmts");
            if (amt != null && amt.length == 7) {
                for (int i = 0; i <= 6; i++) {
                    tanks[i].amount = Math.min(TANK_SIZE, amt[i]);
                }
            }

            byte[] flags = nbt.getByteArray("TankFlags");
            if (flags != null && flags.length == 7) {
                for (int i = 0; i <= 6; i++) {
                    tanks[i].type = TankType.values()[flags[i] % 3];
                }
            }
        }
    }

    public void markFlowPath(EnumFacing inputDir, boolean isFlowing) {
        // get strongest shifter
        EnumSet<EnumFacing> shiftedSides = isFlowing && inputDir != null ? EnumSet.of(inputDir) : EnumSet.noneOf(EnumFacing.class);
        EnumSet<EnumFacing> updateSides = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing facing : EnumFacing.VALUES) {
            int dist = owner.getShifterStrength(facing);
            if (dist > 0) {
                IShifter shifter = owner.getNearestShifter(facing);
                if (shifter != null && shifter.isShifting()) {
                    System.out.println("i am be shifting");
                    shiftedSides.add(facing.getOpposite());
                }
            }
        }

        for (EnumFacing facing : EnumFacing.VALUES) {
            TankType oldType = tanks[facing.ordinal()].type;
            if (shiftedSides.size() > 0) {
                if (owner.connects(facing)) {
                    if (shiftedSides.contains(facing)) {
                        tanks[facing.ordinal()].type = TankType.INPUT;
                    } else {
                        tanks[facing.ordinal()].type = TankType.OUTPUT;
                    }
                } else {
                    tanks[facing.ordinal()].type = TankType.NONE;
                }
            } else {
                tanks[facing.ordinal()].type = TankType.NONE;
            }

            if (facing == EnumFacing.DOWN && owner.connects(facing) && tanks[0].type == TankType.NONE)
                tanks[0].type = TankType.OUTPUT;

            if (oldType != tanks[facing.ordinal()].type) {
                updateSides.add(facing);
            }
        }

        for (EnumFacing facing : updateSides) {
            TilePipe pipe = PipeUtils.getPipe(owner.getWorld(), owner.getPos().offset(facing), facing.getOpposite());
            if (pipe != null) {
                System.out.println(owner.getPos() + " " + facing + " " + tanks[facing.ordinal()].type);
                pipe.fluid.markFlowPath(facing.getOpposite(), tanks[facing.ordinal()].type == TankType.OUTPUT);
            }
        }
    }

    @Override
    public void update() {
        if (owner.getWorld() == null || owner.getWorld().isRemote) {
            return;
        }

        if (fluidStack == null) {
            return;
        }

        CAN_BE_DRAINED = true;

	    int inputAmount = 0;
	    int inputCount = 0;
	    int outputFreeAmount = 0;
	    int outputCount = 0;

	    // count fluids, [O]->Neighbour
	    for (int i = 0; i < 6; i++)
	    	if (tanks[i].type == TankType.INPUT) {
			    inputAmount += tanks[i].amount;
			    inputCount++;
		    } else if (tanks[i].type == TankType.OUTPUT && i < 6) {
                IFluidHandler handler = getTankBlockNeighbor(owner.getPos(), EnumFacing.getFront(i));
                if (handler != null) {
                    FluidHandlerHelper.push(tanks[i], handler, TANK_RATE);
                }
			    outputFreeAmount += tanks[i].getFreeSpace();
			    outputCount++;
		    }

	    if (outputCount > 0 && outputFreeAmount > 0) {
		    // [N]->[O]
		    int pushAmount = tanks[6].amount;
		    for (int i = 0; i < 6; i++) {
			    if (tanks[i].type == TankType.OUTPUT) {
				    int toPush = Math.min(pushAmount * tanks[i].getFreeSpace() / outputFreeAmount, tanks[6].amount);
				    FluidHandlerHelper.push(tanks[6], tanks[i], Math.min(toPush, TANK_RATE));
			    }
		    }
	    }

	    if (inputCount > 0 && inputAmount > 0) {
		    // [I]->[N]
		    int pushFreeSpace = tanks[6].getFreeSpace();
		    for (int i = 0; i < 6; i++) {
			    if (tanks[i].type == TankType.INPUT) {
					int toPush = Math.min(pushFreeSpace * tanks[i].amount / inputAmount, tanks[i].amount);
					FluidHandlerHelper.push(tanks[i], tanks[6], Math.min(toPush, TANK_RATE));
			    }
		    }
	    }

        CAN_BE_DRAINED = false;

        checkPacketUpdate();
    }

    PacketFluidUpdate getSyncPacket(boolean ignoreDirty) {
        return new PacketFluidUpdate(owner, this, ignoreDirty);
    }

    void sendPacket(boolean ignoreDirty) {
        if (owner.getWorld() != null && !owner.getWorld().isRemote) {
            CharsetPipes.instance.packet.sendToAllAround(getSyncPacket(ignoreDirty), owner, CharsetPipes.PIPE_TESR_DISTANCE);
        }
    }

    private void checkPacketUpdate() {
        if (fluidDirty) {
            if (owner.getWorld() != null && !owner.getWorld().isRemote) {
                sendPacket(false);
            }
            return;
        }

        for (int i = 0; i <= 6; i++) {
            if (tanks[i].isDirty()) {
                if (owner.getWorld() != null && !owner.getWorld().isRemote) {
                    sendPacket(false);
                }
                return;
            }
        }
    }

    private void pushAll(EnumFacing pushDir) {
        FluidHandlerHelper.push(tanks[pushDir.ordinal()], getTankBlockNeighbor(owner.getPos(), pushDir), TANK_RATE);
        FluidHandlerHelper.push(tanks[6], tanks[pushDir.ordinal()], TANK_RATE);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != pushDir && owner.connects(facing)) {
                FluidHandlerHelper.push(tanks[facing.ordinal()], tanks[6], TANK_RATE);
            }
        }
    }

    public IFluidHandler getTankBlockNeighbor(BlockPos pos, EnumFacing direction) {
        BlockPos p = pos.offset(direction);
        TileEntity tile = owner.getWorld().getTileEntity(p);
        return CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tile, direction.getOpposite());
    }
}
