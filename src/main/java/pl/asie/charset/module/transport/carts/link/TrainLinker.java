/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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
