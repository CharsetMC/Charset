/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.tweak;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.*;

@CharsetModule(
        name = "tweak.orderedPickup",
        description = "Makes EntityItems picked up in order from oldest to youngest.",
        profile = ModuleProfile.TESTING
)
public class CharsetTweakOrderedPickup {
    private final Object object = new Object();
    private final Map<EntityPlayer, List<EntityItem>> itemsToAdd = new WeakHashMap<>();
    private final ThreadLocal<Integer> processingPlayer = new ThreadLocal<>();
    private final ThreadLocal<Object> isInLoop = new ThreadLocal<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // More effective (but much slower) method would probably be
            // to either check the stacktrace or use a coremod.
            // TODO: Add CharsetPatches logic for doing this faster.
            isInLoop.set(object);
        } else if (event.phase == TickEvent.Phase.END) {
            isInLoop.remove();
            processingPlayer.set(event.player.getEntityId());
            List<EntityItem> list = itemsToAdd.remove(event.player);
            if (list != null) {
                list.sort((a, b) -> b.getAge() - a.getAge());
                for (EntityItem item : list) {
                    item.onCollideWithPlayer(event.player);
                }
            }
            processingPlayer.remove();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        if (isInLoop.get() != null && event.getEntityPlayer() != null) {
            Integer procPlayer = processingPlayer.get();
            if (procPlayer != null && procPlayer != event.getEntityPlayer().getEntityId()) {
                event.setCanceled(true);
                itemsToAdd.computeIfAbsent(event.getEntityPlayer(), (p) -> Collections.synchronizedList(new ArrayList<>())).add(event.getItem());
            }
        }
    }
}
