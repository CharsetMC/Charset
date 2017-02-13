package pl.asie.charset.pipes.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.pipes.PipeUtils;

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
        public final EnumFacing dir;
        public final IShifter shifter;

        private Direction(EnumFacing dir, IShifter shifter) {
            this.dir = dir;
            this.shifter = shifter;
        }

        public boolean test(ItemStack stack) {
            return (shifter == null || (shifter.isShifting() && shifter.matches(stack)));
        }

        public boolean test(FluidStack stack) {
            return (shifter != null && (shifter.isShifting() && shifter.matches(stack)));
        }
    }

    private final TilePipe owner;
    private boolean isPressured;
    private EnumMap<EnumFacing, DirectionType> directions = new EnumMap<EnumFacing, DirectionType>(EnumFacing.class);
    private EnumMap<EnumFacing, IShifter> pressuredDirections = new EnumMap<EnumFacing, IShifter>(EnumFacing.class);

    public PipeLogic(TilePipe owner) {
        this.owner = owner;
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

    public boolean hasPressure(EnumFacing direction) {
        return isPressured ? directions.containsKey(direction) : pressuredDirections.containsKey(direction);
    }

    public void updateDirections() {
        List<EnumFacing> directionList = new ArrayList<EnumFacing>();
        List<EnumFacing> pressureList = new ArrayList<EnumFacing>();

        isPressured = false;

        // Step 1: Make a list of all valid directions, as well as all shifters.
        for (EnumFacing direction : EnumFacing.VALUES) {
            TilePipe pipe = PipeUtils.getPipe(owner.getWorld(), owner.getPos().offset(direction), direction.getOpposite());
            if (pipe != null && pipe.getLogic().hasPressure(direction)) {
                isPressured = true;
                continue;
            }

            if (owner.connects(direction)) {
                directionList.add(direction);
            }

            IShifter p = owner.getNearestShifter(direction);

            if (p != null && p.getDirection() == direction) {
                pressureList.add(direction);
            }
        }

        // Step 2: Sort the shifter list.
        Collections.sort(pressureList, new Comparator<EnumFacing>() {
            @Override
            public int compare(EnumFacing o1, EnumFacing o2) {
                return getInternalShifterStrength(owner.getNearestShifter(o1), o1) - getInternalShifterStrength(owner.getNearestShifter(o2), o2);
            }
        });

        directions.clear();
        pressuredDirections.clear();

        for (EnumFacing direction : directionList) {
            TileEntity tile = owner.getNeighbourTile(direction);
            boolean hasItem = CapabilityHelper.get(Capabilities.ITEM_INSERTION_HANDLER, tile, direction.getOpposite()) != null;
            boolean hasFluid = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tile, direction.getOpposite()) != null;
            DirectionType type = hasItem ? (hasFluid ? DirectionType.DUAL_TARGET : DirectionType.FLUID_TARGET) : (hasFluid ? DirectionType.FLUID_TARGET : null);
            if (type == null) {
                ModCharset.logger.warn(owner.getPos().offset(direction) + " has connection but no DirectionType! Will be ignored.");
            } else {
                directions.put(direction, type);
                if (pressureList.contains(direction)) {
                    pressuredDirections.put(direction, owner.getNearestShifter(direction));
                }
            }
        }
    }
}
