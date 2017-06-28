package pl.asie.charset.lib;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.ThreeState;

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
    private final Map<String, Set<ResourceLocation>> registryLocs = new HashMap<>();

    private CharsetIMC() {

    }

    public void loadConfig(Configuration config) {
        for (String s : config.getStringList("whitelist", "functionalityRegistry", new String[]{}, "Functionality registry whitelist (example entry: carry:minecraft:bedrock)")) {
            String[] sSplit = s.split(":", 2);
            if (sSplit.length >= 2) {
                add("w:" + sSplit[0], new ResourceLocation(sSplit[1]));
            } else {
                ModCharset.logger.warn("Invalid functionality registry config entry: " + s);
            }
        }
        for (String s : config.getStringList("blacklist", "functionalityRegistry", new String[]{}, "Functionality registry blacklist (example entry: carry:minecraft:bedrock)")) {
            String[] sSplit = s.split(":", 2);
            if (sSplit.length >= 2) {
                add("b:" + sSplit[0], new ResourceLocation(sSplit[1]));
            } else {
                ModCharset.logger.warn("Invalid functionality registry config entry: " + s);
            }
        }
        config.get("functionalityRegistry", "whitelist", new String[]{});
    }

    public Collection<ResourceLocation> getResourceLocationEntries(String key) {
        return registryLocs.getOrDefault(key, Collections.emptySet());
    }

    public ThreeState allows(String key, ResourceLocation location) {
        return getResourceLocationEntries("b:" + key).contains(location) ? ThreeState.NO
                : getResourceLocationEntries("w:" + key).contains(location) ? ThreeState.YES
                : ThreeState.MAYBE;
    }

    public ThreeState allows(String key, Set<ResourceLocation> locations) {
        ThreeState result = ThreeState.MAYBE;

        for (ResourceLocation loc : locations) {
            ThreeState newResult = allows(key, loc);
            if (newResult == ThreeState.NO)
                return ThreeState.NO;
            else if (result != ThreeState.YES)
                result = newResult;
        }

        return result;
    }

    private void add(Collection<String> entryKeys, ResourceLocation entry) {
        for (String entryKey : entryKeys) {
            add(entryKey, entry);
        }
    }

    private void add(String entryKey, ResourceLocation entry) {
        if (!registryLocs.containsKey(entryKey))
            registryLocs.put(entryKey, Sets.newHashSet(entry));
        else
            registryLocs.get(entryKey).add(entry);
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
