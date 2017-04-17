package pl.asie.charset.pipes.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PipeLogic {
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

    public static class Direction {
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
    }

    private final TilePipe owner;
    private byte pressuredDirMask;
    private List<Direction> pressureDirections = new ArrayList<>(6);
    private List<Direction> nonPressureDirections = new ArrayList<>(6);

    public PipeLogic(TilePipe owner) {
        this.owner = owner;
    }

    public Collection<Direction> getPressuredDirections() {
        return pressureDirections;
    }

    public Collection<Direction> getNonPressuredDirections() {
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

        pressuredDirMask = 0;

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
                pressuredDirMask |= (1 << direction.ordinal());
                shifterList.add(p);
            }
        }

        // Step 2: Sort the shifter list.
        Collections.sort(shifterList, Comparator.comparingInt(o -> getInternalShifterStrength(o, o.getDirection())));

        pressureDirections.clear();
        for (IShifter shifter : shifterList) {
            DirectionType type = calcDirectionType(shifter.getDirection());
            if (type != null) {
                //noinspection ConstantConditions
                pressureDirections.add(new Direction(type, shifter.getDirection(), shifter));
            }
        }

        nonPressureDirections.clear();
        for (EnumFacing direction : directionSet) {
            DirectionType type = calcDirectionType(direction);
            if (type != null) {
                nonPressureDirections.add(new Direction(type, direction, null));
            }
        }
    }

    public boolean hasPressure(EnumFacing direction) {
        return (pressuredDirMask & (1 << direction.ordinal())) != 0;
    }

    public boolean hasPressure() {
        return pressuredDirMask != 0;
    }
}
