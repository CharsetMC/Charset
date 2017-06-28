package pl.asie.charset.lib.ui;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemHandlerCharset extends ItemStackHandler {
    public ItemHandlerCharset(int size) {
        super(size);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        validateSlotIndex(slot);
        // No equality check here!
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }
}
