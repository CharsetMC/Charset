package pl.asie.charset.lib.scheduler;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

// TODO: Implement saving? Ha, ha, I wish.
public class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();
    private final Map<World, TLongObjectMap<Queue<ScheduledEvent>>> schedule = new WeakHashMap<>();

    public ScheduledEvent in(World world, int ticks, Runnable runnable) {
        return at(world, world.getTotalWorldTime() + ticks, runnable);
    }

    public ScheduledEvent at(World world, long targetTime, Runnable runnable) {
        TLongObjectMap<Queue<ScheduledEvent>> requests = schedule.computeIfAbsent(world, k -> new TLongObjectHashMap<>());
        Queue<ScheduledEvent> queue = requests.get(targetTime);
        if (queue == null) {
            queue = new ArrayDeque<>();
            requests.put(targetTime, queue);
        }
        ScheduledEvent event = new ScheduledEvent(runnable);
        queue.add(event);
        return event;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        schedule.remove(event.getWorld());
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TLongObjectMap<Queue<ScheduledEvent>> requests = schedule.get(event.world);
            if (requests != null) {
                // We tick at the *end* of a phase, so the world time has
                // already had 1 added to it.
                long time = event.world.getTotalWorldTime() - 1;
                Queue<ScheduledEvent> queue = requests.remove(time);
                if (queue != null) {
                    for (ScheduledEvent r : queue) {
                        event.world.profiler.startSection(r.getClass().getName());
                        r.run();
                        event.world.profiler.endSection();
                    }
                }
            }
        }
    }
}
