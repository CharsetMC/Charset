package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DyeableItem implements IDyeableItem, ICapabilityProvider {
    private final boolean storesAlpha;
    private final boolean[] colorSet;
    private final int[] colors;

    public DyeableItem() {
        this(1, false);
    }

    public DyeableItem(int count, boolean alpha) {
        this.colorSet = new boolean[count];
        this.colors = new int[count];
        this.storesAlpha = alpha;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.DYEABLE_ITEM;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.DYEABLE_ITEM ? Capabilities.DYEABLE_ITEM.cast(this) : null;
    }

    @Override
    public int getColorSlotCount() {
        return colors.length;
    }

    @Override
    public int getColor(int slot) {
        if (hasColor(slot)) {
            return colors[slot];
        } else {
            return -1;
        }
    }

    @Override
    public boolean hasColor(int slot) {
        return colorSet[slot];
    }

    @Override
    public boolean removeColor(int slot) {
        if (colorSet[slot]) {
            colorSet[slot] = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean setColor(int slot, int color) {
        colorSet[slot] = true;
        if (!storesAlpha) color |= 0xFF000000;
        colors[slot] = color;
        return true;
    }
}
