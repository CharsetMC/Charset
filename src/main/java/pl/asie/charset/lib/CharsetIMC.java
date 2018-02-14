/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.lib;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.utils.ThreeState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class CharsetIMC {
    public static CharsetIMC INSTANCE = new CharsetIMC();
    private final Multimap<String, ResourceLocation> registryLocs = HashMultimap.create();
    private final Multimap<String, String> registryDomainLocs = HashMultimap.create();

    private CharsetIMC() {

    }

    public void loadConfig(Configuration config) {
        for (String s : ConfigUtils.getStringList(config, "functionalityRegistry", "whitelist", new String[]{}, "Functionality registry whitelist (example entry: carry:minecraft:bedrock)", true)) {
            String[] sSplit = s.split(":", 2);
            if (sSplit.length >= 2) {
                add("w:" + sSplit[0], new ResourceLocation(sSplit[1]));
            } else {
                ModCharset.logger.warn("Invalid functionality registry config entry: " + s);
            }
        }
        for (String s : ConfigUtils.getStringList(config, "functionalityRegistry", "blacklist", new String[]{}, "Functionality registry blacklist (example entry: carry:minecraft:bedrock)", true)) {
            String[] sSplit = s.split(":", 2);
            if (sSplit.length >= 2) {
                add("b:" + sSplit[0], new ResourceLocation(sSplit[1]));
            } else {
                ModCharset.logger.warn("Invalid functionality registry config entry: " + s);
            }
        }
        config.get("functionalityRegistry", "whitelist", new String[]{});
    }

    public ThreeState allows(String key, ResourceLocation location) {
        String bKey = "b:" + key;
        String wKey = "w:" + key;

        if (registryLocs.get(bKey).contains(location)) {
            return ThreeState.NO;
        } else if (registryLocs.get(wKey).contains(location)) {
            return ThreeState.YES;
        } else if (registryDomainLocs.get(bKey).contains(location.getResourceDomain())) {
            return ThreeState.NO;
        } else if (registryDomainLocs.get(wKey).contains(location.getResourceDomain())) {
            return ThreeState.YES;
        } else {
            return ThreeState.MAYBE;
        }
    }

    public ThreeState allows(String key, Collection<ResourceLocation> locations) {
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
        if (entry.getResourcePath().equals("*")) {
            registryDomainLocs.put(entryKey, entry.getResourceDomain());
        } else {
            registryLocs.put(entryKey, entry);
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
