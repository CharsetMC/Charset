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

package pl.asie.charset.lib.misc;

import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemSnow;
import net.minecraft.item.ItemSnowball;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.utils.EntityUtils;

import java.util.HashMap;
import java.util.Map;

import static net.minecraftforge.event.world.NoteBlockEvent.Note.*;

public final class Todokete {
	private static final NoteBlockEvent.Note[] n = {
			E, B, G_SHARP, A, A, G_SHARP, A, G_SHARP, F_SHARP, E,
			E, B, G_SHARP, A,
			A, B, C_SHARP, B, B,
			B, D, C_SHARP, B, A
	};

	public Todokete() {

	}
	private final Map<Integer, Integer> map = new HashMap<>();
	private final ThreadLocal<Integer> currentPlayer = new ThreadLocal<>();

	private float halate(Integer player) {
		int v = map.getOrDefault(player, 0);
		NoteBlockEvent.Note note = n[v];
		map.put(player, (v+1)%n.length);
		int vv = note.ordinal();
		if (vv < NoteBlockEvent.Note.C.ordinal() || v == 16 || v == 20 || v == 21) {
			vv += 12;
		}
		return (float)Math.pow(2.0, (vv - 12) / 12.0);
	}

	@SubscribeEvent
	public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		try { // shhhhhhh
			if (event.getItemStack().getItem() instanceof ItemSnowball) {
				if (event.getEntityPlayer() == null || EntityUtils.isPlayerFake(event.getEntityPlayer())) {
					currentPlayer.set(null);
				} else {
					currentPlayer.set(event.getEntityPlayer().getEntityId());
				}
			} else {
				currentPlayer.set(null);
			}
		} catch (Throwable t) {
			// shhhhh
			try {
				currentPlayer.set(null);
			} catch (Throwable tt) {
				// shhhhhhhhhhhh
			}
		}
	}

	@SubscribeEvent
	public void onPlaySound(PlaySoundAtEntityEvent event) {
		try { // shhhhhhh
			if (event.getSound() == SoundEvents.ENTITY_SNOWBALL_THROW) {
				Integer id = currentPlayer.get();
				if (id != null) {
					currentPlayer.remove();
					event.setPitch(halate(id));
				}
			}
		} catch (Throwable t) {
			// shhhhh
		}
	}
}
