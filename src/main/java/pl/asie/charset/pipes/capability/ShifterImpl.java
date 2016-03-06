package pl.asie.charset.pipes.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.pipes.IShifter;

/**
 * Default implementation for the shifter.
 * @author rubensworks
 */
public class ShifterImpl implements IShifter {

    private Mode mode;
    private EnumFacing direction;
    private int shiftDistance;
    private boolean shifting;
    private boolean hasFilter;
    private ItemStack filter;

    public ShifterImpl(Mode mode, EnumFacing direction, int shiftDistance, boolean isShifting,
                       boolean hasFilter, ItemStack filter) {
        this.mode = mode;
        this.direction = direction;
        this.shiftDistance = shiftDistance;
        this.shifting = isShifting;
        this.hasFilter = hasFilter;
        this.filter = filter;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public EnumFacing getDirection() {
        return direction;
    }

    @Override
    public int getShiftDistance() {
        return shiftDistance;
    }

    @Override
    public boolean isShifting() {
        return shifting;
    }

    @Override
    public boolean hasFilter() {
        return hasFilter;
    }

    public ItemStack getFilter() {
        return filter;
    }

    @Override
    public boolean matches(ItemStack source) {
        return ItemStack.areItemStacksEqual(filter, source);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setDirection(EnumFacing direction) {
        this.direction = direction;
    }

    public void setShiftDistance(int shiftDistance) {
        this.shiftDistance = shiftDistance;
    }

    public void setShifting(boolean shifting) {
        this.shifting = shifting;
    }

    public void setHasFilter(boolean hasFilter) {
        this.hasFilter = hasFilter;
    }

    public void setFilter(ItemStack filter) {
        this.filter = filter;
    }

}
