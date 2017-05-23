package pl.asie.charset.module.transport.carts.link;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrainLinker {
	private final BiMap<UUID, Linkable> linkableMap;

	public TrainLinker() {
		linkableMap = HashBiMap.create();
	}

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityMinecart) {
			Linkable link = new Linkable(event.getObject());
			event.addCapability(Linkable.ID, Linkable.PROVIDER.get().create(link));
			linkableMap.put(link.getId(), link);
		}
	}

	private double getDistanceXZ(Entity one, Entity two) {
		double a = one.posX - two.posX;
		double b = one.posZ - two.posZ;
		return a*a+b*b;
	}

	@SubscribeEvent
	public void onWorldTickEnd(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (Linkable link : linkableMap.values()) {
				if (link.previous == null && link.next != null) {
					List<Linkable> linked = new ArrayList<>();
					Entity front = link.getOwner();
					linked.add(link);
					while (link.next != null) {
						link = link.next;
						linked.add(link);
					}
				}
			}
		}
	}

	public Linkable get(Entity entity) {
		return get(entity.getPersistentID());
	}

	public Linkable get(UUID id) {
		Linkable link = linkableMap.get(id);

		if (link.getOwner().isDead) {
			if (link.previous != null) unlink(link, link.previous);
			if (link.next != null) unlink(link, link.next);
			linkableMap.remove(id);
			return null;
		}

		return link;
	}

	public void link(Linkable first, Linkable second) {
		first.next = second;
		second.previous = first;
	}

	public boolean unlink(Linkable first, Linkable second) {
		if (first.previous == second) {
			first.previous = null;
			second.next = null;
			return true;
		} else if (first.next == second) {
			first.next = null;
			second.previous = null;
			return true;
		} else {
			return false;
		}
	}
}
