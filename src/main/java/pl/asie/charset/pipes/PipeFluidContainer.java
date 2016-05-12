package pl.asie.charset.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.*;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.ModCharsetLib;

import java.util.*;

public class PipeFluidContainer implements IFluidHandler, ITickable {
    public class Tank implements IFluidTank {
        public final EnumFacing location;
        FluidStack stack;
        int color;
        private boolean dirty;

        public Tank(EnumFacing location) {
            this.location = location;
        }

        public FluidStack get() {
            return stack;
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

        public int add(FluidStack in, boolean simulate) {
            if (stack == null) {
                int targetAmount = Math.min(in.amount, TANK_SIZE);
                if (!simulate) {
                    if (targetAmount > 0) {
                        stack = in.copy();
                        stack.amount = targetAmount;
                    } else {
                        stack = null;
                    }
                    dirty = true;
                }
                return targetAmount;
            } else if (stack.isFluidEqual(in)) {
                int targetAmount = Math.min(in.amount, TANK_SIZE - stack.amount);
                if (targetAmount > 0) {
                    if (!simulate) {
                        stack.amount += targetAmount;
                        dirty = true;
                    }
                    return targetAmount;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }

        public FluidStack remove(int amount, boolean simulate) {
            if (stack != null) {
                int targetAmount = Math.min(stack.amount, amount);
                FluidStack out = stack.copy();
                out.amount = targetAmount;
                if (!simulate) {
                    if (stack.amount == targetAmount) {
                        stack = null;
                    } else {
                        stack.amount -= targetAmount;
                    }
                    dirty = true;
                }
                return out;
            } else {
                return null;
            }
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return add(resource, !doFill);
        }

        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return stack != null && stack.isFluidEqual(resource) ? remove(resource.amount, !doDrain) : null;
        }

        public FluidStack drain(int maxDrain, boolean doDrain) {
            return remove(maxDrain, !doDrain);
        }

        public boolean canFill(Fluid fluid) {
            return stack == null || stack.getFluid() == fluid;
        }

        public boolean canDrain(Fluid fluid) {
            return stack != null && stack.getFluid() == fluid;
        }

        @Override
        public FluidStack getFluid() {
            return get();
        }

        @Override
        public int getFluidAmount() {
            return stack != null ? stack.amount : 0;
        }

        @Override
        public int getCapacity() {
            return TANK_SIZE;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(stack, TANK_SIZE);
        }
    }

    public static final int TANK_RATE = 80;
    final Tank[] tanks = new Tank[7];

    private static final int TANK_SIZE = 250;
    private final PartPipe owner;

    public PipeFluidContainer(PartPipe owner) {
        this.owner = owner;
        for (int i = 0; i < 7; i++) {
            tanks[i] = new Tank(i < 6 ? EnumFacing.getFront(i) : null);
        }
    }

    @Override
    public void update() {
        if (owner.getWorld() == null || owner.getWorld().isRemote) {
            return;
        }

        EnumFacing pushDir = null;
        IShifter shifter = null;
        int shifterDist = Integer.MAX_VALUE;

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (owner.connects(facing)) {
                int sStr = owner.getShifterStrength(facing);
                if (sStr > 0 && sStr < shifterDist) {
                    IShifter s = owner.getNearestShifter(facing);
                    if (s != null && s.isShifting()) {
                        pushDir = facing;
                        shifter = s;
                        shifterDist = sStr;
                    }
                }
            }
        }

        if (pushDir != null) {
            pushAll(pushDir, shifter);
        } else if (owner.connects(EnumFacing.DOWN)) {
            pushAll(EnumFacing.DOWN, null);
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

        for (int i = 0; i <= 6; i++) {
            if (tanks[i].isDirty()) {
                if (owner.getWorld() != null && !owner.getWorld().isRemote) {
                    ModCharsetPipes.packet.sendToAllAround(new PacketFluidUpdate(owner, this), owner, ModCharsetPipes.PIPE_TESR_DISTANCE);
                }
                break;
            }
        }
    }

    private void pushAll(EnumFacing pushDir, IShifter shifter) {
        push(tanks[pushDir.ordinal()], getTankBlockNeighbor(owner.getPos(), pushDir), pushDir.getOpposite(), TANK_RATE);
        push(tanks[6], tanks[pushDir.ordinal()], TANK_RATE);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != pushDir && owner.connects(facing)) {
                push(tanks[facing.ordinal()], tanks[6], TANK_RATE);
            }
        }
    }

    private void push(Tank from, Tank to, int maxAmount) {
        if (from.get() == null || !to.canFill(from.get().getFluid())) {
            return;
        }

        FluidStack out = from.stack.copy();
        out.amount = Math.min(out.amount, maxAmount);
        if (out.amount > 0) {
            int amt = to.fill(out, true);
            from.remove(amt, false);
        }
    }

    private void push(Tank from, IFluidHandler to, EnumFacing toSide, int maxAmount) {
        if (from.get() == null || !to.canFill(toSide, from.get().getFluid())) {
            return;
        }

        FluidStack out = from.stack.copy();
        out.amount = Math.min(out.amount, maxAmount);
        if (out.amount > 0) {
            int amt = to.fill(toSide, out, true);
            from.remove(amt, false);
        }
    }

    public IFluidHandler getTankBlockNeighbor(BlockPos pos, EnumFacing direction) {
        BlockPos p = pos.offset(direction);
        PartPipe pipe = PipeUtils.getPipe(owner.getWorld(), p, direction.getOpposite());
        if (pipe != null) {
            return pipe.fluid;
        } else {
            TileEntity tile = owner.getWorld().getTileEntity(p);
            if (tile instanceof IFluidHandler) {
                return ((IFluidHandler) tile);
            }
        }

        return null;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount == 0 || !canFill(from, resource.getFluid())) {
            return 0;
        }

        return tanks[from.ordinal()].fill(resource, doFill);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        Tank tank = tanks[from == null ? 6 : from.ordinal()];
        return owner.connects(from) && tank.canFill(fluid);
    }

    // Super-Advanced Fluid Shifting Algorithm
    // Maybe I'll finish it one day - but it might be slow/laggy/whatnot.

    /* public IFluidHandler getTankNeighbor(IFluidHandler handler, EnumFacing direction) {
        if (handler instanceof Tank) {
            EnumFacing loc = ((Tank) handler).location;
            if (loc == null) {
                return tanks[direction.ordinal()];
            } else if (direction == loc) {
                return getTankBlockNeighbor(direction);
            } else if (direction == loc.getOpposite()) {
                return tanks[6];
            } else {
                return null;
            }
        }

        return null;
    }

    private FluidStack fillTank(Tank tank, FluidStack resource, boolean doFill) {
        if (tank.get() != null) {
            if (tank.get().isFluidEqual(resource)) {
                int amount = Math.min(resource.amount, TANK_MAX_INSERT);
                if (tank.get().amount + amount > TANK_SIZE) {
                    FluidStack shifted = resource.copy();
                    shifted.amount = tank.get().amount + amount - resource.amount;
                    if (doFill) {
                        tank.get().amount = TANK_SIZE;
                    }
                    return shifted;
                } else {
                    if (doFill) {
                        tank.get().amount += amount;
                    }
                    return null;
                }
            } else {
                FluidStack shifted = tank.stack;
                tank.stack = resource;
                return shifted;
            }
        } else {
            if (resource.amount > TANK_MAX_INSERT) {
                FluidStack out = resource.copy();
                out.amount = TANK_MAX_INSERT;
                tank.add(out, !doFill);
                return null;
            } else {
                tank.add(resource, !doFill);
                return null;
            }
        }
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount == 0 || from == null || !owner.connects(from)) {
            return 0;
        }

        FluidStack shifted = null;
        Tank tank = tanks[from.ordinal()];
        EnumFacing tankDirection = from.getOpposite();
        PipeFluidContainer tankOwner = this;
        shifted = fillTank(tank, resource, doFill);
        while (shifted != null) {
            // Advance tank.
            if (tank.location == null) {
                tank = tankOwner.tanks[tankDirection.ordinal()];
            } else if (tank.location == tankDirection.getOpposite()) {
                tank = tankOwner.tanks[6];
            } else if (tank.location == tankDirection) {
                IFluidHandler nextTank = getTankBlockNeighbor(tankOwner.owner.getPos(), tankDirection);
                if (nextTank instanceof PipeFluidContainer) {
                    tankOwner = (PipeFluidContainer) nextTank;
                    if (tankOwner.owner.connects(tankDirection.getOpposite())) {
                        tank = tankOwner.tanks[tankDirection.getOpposite().ordinal()];
                    } else {
                        return shifted.amount;
                    }
                } else {
                    return nextTank.fill(tankDirection.getOpposite(), shifted, doFill);
                }
            }

            // Fill tank.
            shifted = fillTank(tank, shifted, doFill);
        }
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return fluid != null && owner.connects(from);
    } */

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return new FluidTankInfo[]{
                tanks[from == null ? 6 : from.ordinal()].getInfo()
        };
    }

    // Ha! Cannot drain me, for I drain myself just fine!

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return false;
    }
}
