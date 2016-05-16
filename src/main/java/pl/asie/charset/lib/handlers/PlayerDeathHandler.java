package pl.asie.charset.lib.handlers;

import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerDeathHandler {
	private Predicate<ItemStack> itemKeepPredicate;

	public PlayerDeathHandler() {
	}

	public boolean hasPredicate() {
		return itemKeepPredicate != null;
	}

	public void addPredicate(Predicate<ItemStack> predicate) {
		if (itemKeepPredicate == null) {
			itemKeepPredicate = predicate;
		} else {
			itemKeepPredicate = Predicates.or(itemKeepPredicate, predicate);
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			for (int i = 0; i < Math.min(event.getEntityPlayer().inventory.getSizeInventory(), event.getOriginal().inventory.getSizeInventory()); i++) {
				ItemStack s = event.getOriginal().inventory.getStackInSlot(i);
				if (s != null && itemKeepPredicate.apply(s)) {
					event.getEntityPlayer().inventory.setInventorySlotContents(i, s);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(PlayerDropsEvent event) {
		String name = event.getEntityPlayer().getName();
		Iterator<EntityItem> itemIterator = event.getDrops().iterator();

		while (itemIterator.hasNext()) {
			EntityItem entityItem = itemIterator.next();

			if (entityItem == null || entityItem.getEntityItem() == null) {
				continue;
			}

			ItemStack stack = entityItem.getEntityItem();
			if (itemKeepPredicate.apply(stack)) {
				itemIterator.remove();

				event.getEntityPlayer().inventory.addItemStackToInventory(stack);
			}
		}
	}
}
