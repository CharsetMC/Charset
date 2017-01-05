package pl.asie.charset.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.*;

public final class CharsetIMC {
    public enum Result {
        YES,
        MAYBE,
        NO;

        public Result or(Result other) {
            if (this == YES || other == YES)
                return YES;
            else if (this == NO || other == NO)
                return NO;
            else
                return MAYBE;
        }
    }

    public static CharsetIMC INSTANCE = new CharsetIMC();
    private static final Map<String, Set<ResourceLocation>> registryLocs = new HashMap<>();

    private CharsetIMC() {

    }

    public Collection<ResourceLocation> getResourceLocationEntries(String key) {
        return registryLocs.getOrDefault(key, Collections.emptySet());
    }

    public Result allows(String key, ResourceLocation location) {
        return getResourceLocationEntries("b:" + key).contains(location) ? Result.NO
                : getResourceLocationEntries("w:" + key).contains(location) ? Result.YES
                : Result.MAYBE;
    }

    public Result allows(String key, Set<ResourceLocation> locations) {
        Result result = Result.MAYBE;

        for (ResourceLocation loc : locations) {
            Result newResult = allows(key, loc);
            if (newResult == Result.NO)
                break;
            else if (result != Result.YES)
                result = newResult;
        }

        return result;
    }

    private void add(String entryKey, ResourceLocation entry) {
        if (!registryLocs.containsKey(entryKey))
            registryLocs.put(entryKey, new HashSet<>());
        registryLocs.get(entryKey).add(entry);
    }

    protected void receiveMessage(FMLInterModComms.IMCMessage msg) {
        if (msg.key.startsWith("add")) {
            String entryKey = msg.key.substring("add".length());
            entryKey = "w:" + entryKey.substring(0, 1).toLowerCase() + entryKey.substring(1);

            if (msg.isResourceLocationMessage()) {
                add(entryKey, msg.getResourceLocationValue());
            }
        } else if (msg.key.startsWith("remove")) {
            String entryKey = msg.key.substring("remove".length());
            entryKey = "b:" + entryKey.substring(0, 1).toLowerCase() + entryKey.substring(1);

            if (msg.isResourceLocationMessage()) {
                add(entryKey, msg.getResourceLocationValue());
            }
        }
    }
}
