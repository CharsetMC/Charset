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
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.pipes.ModCharsetPipes;

import javax.annotation.Nullable;

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

    public class Tank implements IFluidHandler {
        public final EnumFacing location;
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
            for (int i = 0; i <= 6; i++) {
                amt[i] = tanks[i].amount;
            }
            nbt.setIntArray("TankAmts", amt);
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

        EnumFacing pushDir = null;
        int shifterDist = Integer.MAX_VALUE;

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (owner.connects(facing)) {
                int sStr = owner.getShifterStrength(facing);
                if (sStr > 0 && sStr < shifterDist) {
                    IShifter s = owner.getNearestShifter(facing);
                    if (s != null && s.isShifting() && s.matches(fluidStack)) {
                        pushDir = facing;
                        shifterDist = sStr;
                    }
                }
            } else {
                tanks[facing.ordinal()].amount = 0;
            }
        }

        CAN_BE_DRAINED = true;

        if (pushDir != null) {
            pushAll(pushDir);
        } else if (owner.connects(EnumFacing.DOWN)) {
            pushAll(EnumFacing.DOWN);
        } else {
            /* FluidStack baseStack = tanks[6].get();
            if (baseStack == null) {
                List<EnumFacing> dirs = new ArrayList<EnumFacing>(6);
                for (EnumFacing facing : EnumFacing.VALUES) {
                    dirs.add(facing);
                }
                Collections.shuffle(dirs);
                for (EnumFacing facing : dirs) {
                    if (tanks[facing.ordinal()].get() != null) {
                        baseStack = tanks[facing.ordinal()].get();
                        break;
                    }
                }
            }

            if (baseStack != null) {
                float amount = baseStack.amount;
                Set<Tank> tankSet = new HashSet<Tank>();

                for (int i = 0; i <= 6; i++) {
                    if (i == 6 || owner.connects(EnumFacing.getFront(i))) {
                        Tank tank = tanks[i];
                        if (tank.get() != null && tank.get().isFluidEqual(baseStack)) {
                            tankSet.add(tank);
                            amount += tank.get().amount;
                        } else if (tank.get() == null) {
                            tankSet.add(tank);
                        }
                    }
                }

                int tankCount = tankSet.size();

                if (amount > 0) {
                    for (Tank tank : tankSet) {
                        int amt = Math.round(amount / tankCount);
                        if (amt > 0) {
                            if (tank.stack != null && tank.stack.amount != amt) {
                                tank.stack.amount = amt;
                                tank.dirty = true;
                            } else if (tank.stack == null) {
                                tank.stack = baseStack.copy();
                                tank.stack.amount = amt;
                                tank.dirty = true;
                            }
                        } else {
                            if (tank.stack != null) {
                                tank.stack = null;
                                tank.dirty = true;
                            }
                        }
                        amount -= amt;
                        tankCount--;
                    }
                }

                if (amount > 0) {
                    ModCharsetLib.logger.warn(String.format("[PipeFluidContainer->equalize] Accidentally voided %.3f mB at %s!", amount, owner.getPos().toString()));
                }
            } */
        }

        CAN_BE_DRAINED = false;

        checkPacketUpdate();
    }

    void sendPacket(boolean ignoreDirty) {
        if (owner.getWorld() != null && !owner.getWorld().isRemote) {
            ModCharsetPipes.packet.sendToAllAround(new PacketFluidUpdate(owner, this, ignoreDirty), owner, ModCharsetPipes.PIPE_TESR_DISTANCE);
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
        FluidUtils.push(tanks[pushDir.ordinal()], getTankBlockNeighbor(owner.getPos(), pushDir), TANK_RATE);
        FluidUtils.push(tanks[6], tanks[pushDir.ordinal()], TANK_RATE);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != pushDir && owner.connects(facing)) {
                FluidUtils.push(tanks[facing.ordinal()], tanks[6], TANK_RATE);
            }
        }
    }

    public IFluidHandler getTankBlockNeighbor(BlockPos pos, EnumFacing direction) {
        BlockPos p = pos.offset(direction);
        TileEntity tile = owner.getWorld().getTileEntity(p);
        return CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tile, direction.getOpposite());
    }
}
