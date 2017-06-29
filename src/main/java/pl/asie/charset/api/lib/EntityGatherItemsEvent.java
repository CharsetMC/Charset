package pl.asie.charset.api.lib;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collection;

/**
 * This is an event used by Charset to gather the ItemStacks an Entity is
 * wearing (for example, armor slots or Baubles slots), as well as ItemStacks
 * an Entity is holding (for example, main hand and off hand slots).
 */
public class EntityGatherItemsEvent extends Event {
    private final boolean collectHeld, collectWorn;
    private final Entity entity;
    private final Collection<ItemStack> stacks;

    public EntityGatherItemsEvent(Entity entity, Collection<ItemStack> stacks, boolean collectHeld, boolean collectWorn) {
        this.entity = entity;
        this.stacks = stacks;
        this.collectHeld = collectHeld;
        this.collectWorn = collectWorn;
    }

    public Entity getEntity() {
        return entity;
    }

    public void addStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }

    public boolean collectsHeld() {
        return collectHeld;
    }

    public boolean collectsWorn() {
        return collectWorn;
    }
}
