package pl.asie.charset.lib;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                return Result.NO;
            else if (result != Result.YES)
                result = newResult;
        }

        return result;
    }

    private void add(Collection<String> entryKeys, ResourceLocation entry) {
        for (String entryKey : entryKeys) {
            if (!registryLocs.containsKey(entryKey))
                registryLocs.put(entryKey, new HashSet<>());
            registryLocs.get(entryKey).add(entry);
        }
    }

    private String toEntryKey(String entryKey, String prefix) {
        entryKey = entryKey.trim();
        return prefix + entryKey.substring(0, 1).toLowerCase() + entryKey.substring(1);
    }

    private List<String> toList(String entryKey, String prefix) {
        if (entryKey.startsWith("[") && entryKey.endsWith("]")) {
            List<String> keys = new ArrayList<>();
            for (String key : entryKey.substring(1, entryKey.length() - 1).split(",")) {
                keys.add(toEntryKey(key, prefix));
            }
            return keys;
        } else {
            return Lists.newArrayList(toEntryKey(entryKey, prefix));
        }
    }

    public void receiveMessage(FMLInterModComms.IMCMessage msg) {
        for (String key : msg.key.split(";")) {
            key = key.trim();
            if (key.startsWith("add")) {
                List<String> entryKeys = toList(key.substring("add".length()), "w:");

                if (msg.isResourceLocationMessage()) {
                    add(entryKeys, msg.getResourceLocationValue());
                }
            } else if (key.startsWith("remove")) {
                List<String> entryKeys = toList(key.substring("remove".length()), "b:");

                if (msg.isResourceLocationMessage()) {
                    add(entryKeys, msg.getResourceLocationValue());
                }
            }
        }
    }
}
