package pl.asie.charset.lib;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

// TODO: Implement saving? Ha, ha, I wish.
public class WorldScheduler {
    public static final WorldScheduler INSTANCE = new WorldScheduler();
    private final Map<World, TLongObjectMap<Queue<Runnable>>> schedule = new WeakHashMap<>();

    public void in(World world, int ticks, Runnable runnable) {
        at(world, world.getTotalWorldTime() + ticks, runnable);
    }

    public void at(World world, long targetTime, Runnable runnable) {
        TLongObjectMap<Queue<Runnable>> requests = schedule.computeIfAbsent(world, k -> new TLongObjectHashMap<>());
        Queue<Runnable> queue = requests.get(targetTime);
        if (queue == null) {
            queue = new ArrayDeque<>();
            requests.put(targetTime, queue);
        }
        queue.add(runnable);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        schedule.remove(event.getWorld());
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TLongObjectMap<Queue<Runnable>> requests = schedule.get(event.world);
            if (requests != null) {
                // We tick at the *end* of a phase, so the world time has
                // already had 1 added to it.
                long time = event.world.getTotalWorldTime() - 1;
                Queue<Runnable> queue = requests.remove(time);
                if (queue != null) {
                    for (Runnable r : queue) {
                        event.world.profiler.startSection(r.getClass().getName());
                        r.run();
                        event.world.profiler.endSection();
                    }
                }
            }
        }
    }
}
