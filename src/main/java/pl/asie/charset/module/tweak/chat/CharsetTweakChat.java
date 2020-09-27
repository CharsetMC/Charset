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

package pl.asie.charset.module.tweak.chat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.EntityUtils;

import java.awt.*;

@CharsetModule(
        name = "tweak.chat",
        description = "Various chat tweaks",
        profile = ModuleProfile.STABLE,
        isDefault = false
)
public class CharsetTweakChat {
    @CharsetModule.Configuration
    public static Configuration config;

    public static String shoutPrefix;
    public static int chatRadius;
    public static boolean chatRadiusEnableShout, enableChatRadius, enableGreentext, enableColoredChat;

    private String applyColors(String playerMsgStr) {
        for (int i = 0; i < playerMsgStr.length() - 1; i++) {
            if (playerMsgStr.charAt(i) == '&') {
                String comp =  "\u00a7" + playerMsgStr.charAt(i + 1);
                for (TextFormatting formatting : TextFormatting.values()) {
                    if (comp.equals(formatting.toString())) {
                        playerMsgStr = playerMsgStr.substring(0, i) + "\u00a7" + playerMsgStr.substring(i + 1);
                        break;
                    }
                }
                break;
            }
        }
        return playerMsgStr;
    }

    @Mod.EventHandler
    public void loadConfig(CharsetLoadConfigEvent event) {
        enableGreentext = ConfigUtils.getBoolean(config, "features","greentext", true, "Enables >implications, I suppose.", false);
        enableColoredChat = ConfigUtils.getBoolean(config, "features", "coloredChat", false, "Colored chat! We all love colored chat, don't we? &cOf course we do!", false);
        enableChatRadius = ConfigUtils.getBoolean(config, "features", "chatDistanceLimit", false, "Adds a distance limit system for chat.", false);

        chatRadius = ConfigUtils.getInt(config, "chatDistanceLimit", "radius", 32, 0, Integer.MAX_VALUE, "The maximum chat distance in blocks.", false);
        chatRadiusEnableShout = ConfigUtils.getBoolean(config, "chatDistanceLimit", "shoutEnabled", true, "Is the shout prefix (!msg) enabled?", false);
        shoutPrefix = applyColors(ConfigUtils.getString(config, "chatDistanceLimit", "shoutPrefix", "&e[Shout]", "Prefix for shout messages.", false));
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        if (!EntityUtils.isPlayerFake(event.getPlayer())) {
            ITextComponent message = event.getComponent();
            if (message instanceof TextComponentTranslation
                    && ((TextComponentTranslation) message).getFormatArgs().length == 2
                    && "chat.type.text".equals(((TextComponentTranslation) message).getKey())) {
                TextComponentTranslation messageTr = (TextComponentTranslation) message;
                Object playerMessage = messageTr.getFormatArgs()[1];
                if (playerMessage instanceof TextComponentString) {
                    String playerMsgStr = ((TextComponentString) playerMessage).getText();

                    for (int i = 0; i < playerMsgStr.length() - 1; i++) {
                        if (enableGreentext) {
                            if (playerMsgStr.charAt(i) == '>'
                                    && (i == 0 || Character.isWhitespace(playerMsgStr.codePointAt(i - 1)))
                                    && Character.isLetterOrDigit(playerMsgStr.codePointAt(i + 1))) {
                                playerMsgStr = playerMsgStr.substring(0, i) + TextFormatting.GREEN + playerMsgStr.substring(i);
                                break;
                            }
                        }

                        if (enableColoredChat) {
                            if (playerMsgStr.charAt(i) == '&') {
                                String comp =  "\u00a7" + playerMsgStr.charAt(i + 1);
                                for (TextFormatting formatting : TextFormatting.values()) {
                                    if (comp.equals(formatting.toString())) {
                                        playerMsgStr = playerMsgStr.substring(0, i) + "\u00a7" + playerMsgStr.substring(i + 1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }

                    boolean isShout = false;
                    if (enableChatRadius && chatRadiusEnableShout && playerMsgStr.startsWith("!")) {
                        isShout = true;
                        playerMsgStr = playerMsgStr.substring(1);
                    }

                    ITextComponent msgResult = new TextComponentTranslation(
                            messageTr.getKey(),
                            messageTr.getFormatArgs()[0],
                            new TextComponentString(playerMsgStr)
                    );

                    if (isShout) {
                        msgResult = new TextComponentTranslation(
                                "chat.charset.shout",
                                shoutPrefix,
                                msgResult
                        );
                    }

                    if (enableChatRadius) {
                        event.setCanceled(true);
                        for (EntityPlayer player : event.getPlayer().getServerWorld().playerEntities) {
                            if (chatRadius <= 0 || player.getDistance(event.getPlayer()) <= chatRadius) {
                                player.sendMessage(msgResult);
                            }
                        }
                    } else {
                        event.setComponent(msgResult);
                    }
                }
            }
        }
    }
}
