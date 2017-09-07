/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tweaks.chat;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.PlayerUtils;

@CharsetModule(
        name = "tweak.chat",
        description = "Various chat tweaks",
        profile = ModuleProfile.STABLE,
        isDefault = false
)
public class CharsetTweakChat {
    @CharsetModule.Configuration
    public static Configuration config;

    public static boolean enableGreentext;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        enableGreentext = config.getBoolean("greentext", "features", true, "Enables >implications, I suppose.");
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        if (!PlayerUtils.isFake(event.getPlayer())) {
            String message = event.getMessage();

            for (int i = 0; i < message.length() - 1; i++) {
                if (message.charAt(i) == '>'
                        && (i == 0 || Character.isWhitespace(message.codePointAt(i - 1)))
                        && Character.isLetterOrDigit(message.codePointAt(i + 1))) {
                    message = message.substring(0, i) + TextFormatting.GREEN + message.substring(i);
                    break;
                }
            }

            String messageFull = "<" + event.getUsername() + "> " + message;
            event.setComponent(new TextComponentString(messageFull));
        }
    }
}
