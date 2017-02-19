package pl.asie.charset.lib;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import pl.asie.charset.lib.utils.TriResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CharsetIMC {

    public static CharsetIMC INSTANCE = new CharsetIMC();
    private static final Map<String, Set<ResourceLocation>> registryLocs = new HashMap<>();

    private CharsetIMC() {

    }

    public Collection<ResourceLocation> getResourceLocationEntries(String key) {
        return registryLocs.getOrDefault(key, Collections.emptySet());
    }

    public TriResult allows(String key, ResourceLocation location) {
        return getResourceLocationEntries("b:" + key).contains(location) ? TriResult.NO
                : getResourceLocationEntries("w:" + key).contains(location) ? TriResult.YES
                : TriResult.MAYBE;
    }

    public TriResult allows(String key, Set<ResourceLocation> locations) {
        TriResult result = TriResult.MAYBE;

        for (ResourceLocation loc : locations) {
            TriResult newResult = allows(key, loc);
            if (newResult == TriResult.NO)
                return TriResult.NO;
            else if (result != TriResult.YES)
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
