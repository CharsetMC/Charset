package pl.asie.charset.pipes.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PipeLogic implements INBTSerializable<NBTTagCompound> {
    public enum DirectionType {
        ITEM_TARGET(true, false),
        FLUID_TARGET(false, true),
        DUAL_TARGET(true, true);

        final boolean acceptsItems, acceptsFluids;

        DirectionType(boolean acceptsItems, boolean acceptsFluids) {
            this.acceptsItems = acceptsItems;
            this.acceptsFluids = acceptsFluids;
        }
    }

    public static class Direction implements Comparable<Direction> {
        public final DirectionType type;
        public final EnumFacing dir;
        public final IShifter shifter;

        private Direction(@Nonnull DirectionType type, @Nonnull EnumFacing dir, @Nullable IShifter shifter) {
            this.type = type;
            this.dir = dir;
            this.shifter = shifter;
        }

        public boolean hasPressure() {
            return shifter != null;
        }

        private boolean canShift() {
            return shifter != null && shifter.isShifting() && shifter.getDirection() == dir;
        }

        public boolean test(ItemStack stack) {
            return type.acceptsItems && (shifter == null || (canShift() && shifter.matches(stack)));
        }

        public boolean test(FluidStack stack) {
            return type.acceptsFluids && (shifter != null && (canShift() && shifter.matches(stack)));
        }

        @Override
        public int compareTo(Direction direction) {
            return dir.compareTo(direction.dir);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Direction)) {
                return false;
            }

            Direction od = (Direction) o;
            return this.dir == od.dir && this.type == od.type && Objects.equals(this.shifter, od.shifter);
        }

        @Override
        public int hashCode() {
            return (this.type.ordinal() * 3 + this.dir.ordinal()) * 7 + (this.shifter != null ? this.shifter.hashCode() : 0);
        }
    }

    private final TilePipe owner;
    private Direction[] pressureDirections = new Direction[6];
    private Direction[] nonPressureDirections = new Direction[6];
    private EnumFacing[] roundRobinPosition = new EnumFacing[6];

    public PipeLogic(TilePipe owner) {
        this.owner = owner;
    }

    public Direction[] getPressuredDirections() {
        return pressureDirections;
    }

    public Direction[] getNonPressuredDirections() {
        return nonPressureDirections;
    }

    // This version takes priority into account (filtered shifters are
    // prioritized over unfiltered shifters at the same distance).
    private int getInternalShifterStrength(IShifter shifter, EnumFacing dir) {
        if (shifter == null) {
            return 0;
        } else {
            return owner.getShifterStrength(dir) * 2 + (shifter.hasFilter() ? 0 : 1);
        }
    }

    private DirectionType calcDirectionType(EnumFacing direction) {
        TileEntity tile = owner.getNeighbourTile(direction);
        boolean hasItem = CapabilityHelper.get(Capabilities.ITEM_INSERTION_HANDLER, tile, direction.getOpposite()) != null;
        boolean hasFluid = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tile, direction.getOpposite()) != null;
        DirectionType type = hasItem ? (hasFluid ? DirectionType.DUAL_TARGET : DirectionType.ITEM_TARGET) : (hasFluid ? DirectionType.FLUID_TARGET : null);
        if (type == null) {
            System.out.println("Type null! " + (tile != null ? tile.getClass().getName() : "null") + " " + owner.getPos().offset(direction) + " " + direction.getOpposite().getName());
        }
        return type;
    }

    public void updateDirections() {
        System.out.println("Update " + owner.getPos());

        Set<EnumFacing> directionSet = EnumSet.noneOf(EnumFacing.class);
        List<IShifter> shifterList = new ArrayList<>(6);

        // Step 1: Make a list of all valid directions, as well as all shifters.
        for (EnumFacing direction : EnumFacing.VALUES) {
            // TilePipe pipe = PipeUtils.getPipe(owner.getWorld(), owner.getPos().offset(direction), direction.getOpposite());
            if (owner.connects(direction)) {
                directionSet.add(direction);
            }

            IShifter p = owner.getNearestShifter(direction);

            if (p != null && p.getDirection() == direction) {
                shifterList.add(p);
            }
        }

        for (int i = 0; i < 6; i++) {
            pressureDirections[i] = null;
            nonPressureDirections[i] = null;
        }

        for (IShifter shifter : shifterList) {
            DirectionType type = calcDirectionType(shifter.getDirection());
            if (type != null) {
                //noinspection ConstantConditions
                pressureDirections[shifter.getDirection().ordinal()] = new Direction(type, shifter.getDirection(), shifter);
            }
        }

        for (EnumFacing direction : directionSet) {
            DirectionType type = calcDirectionType(direction);
            if (type != null) {
                nonPressureDirections[direction.ordinal()] = new Direction(type, direction, null);
            }
        }
    }

    public boolean hasPressure(EnumFacing direction) {
        return pressureDirections[direction.ordinal()] != null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        int rrp = 0;
        for (int i = 0; i < 6; i++) {
            if (roundRobinPosition[i] != null) {
                rrp |= (roundRobinPosition[i].ordinal() << (i * 3));
            }
        }
        nbt.setInteger("rrp", rrp);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (int i = 0; i < 6; i++) {
            roundRobinPosition[i] = EnumFacing.DOWN;
        }

        int rrp = nbt != null ? nbt.getInteger("rrp") : -1;
        if (rrp >= 0) {
            for (int i = 0; i < 6; i++) {
                roundRobinPosition[i] = EnumFacing.getFront((rrp >> (i * 3) & 7));
            }
        }
    }

    public EnumFacing getRoundRobinPosition(EnumFacing input) {
        EnumFacing facing = this.roundRobinPosition[input.ordinal()];
        return facing == null ? EnumFacing.DOWN : facing;
    }

    public void setRoundRobinPosition(EnumFacing input, EnumFacing roundRobinPosition) {
        this.roundRobinPosition[input.ordinal()] = roundRobinPosition;
    }
}
