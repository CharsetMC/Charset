/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;

public class SubCommandFog extends SubCommand {
    public SubCommandFog() {
        super("fog", Side.CLIENT);
    }

    @Override
    public String getUsage() {
        return "Change rendering distance: x, -x or +x";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        int value = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
        if (args.length > 0) {
            int newValue;
            if (args[0].startsWith("-")) {
                if (args[0].length() == 1) {
                    newValue = value - 1;
                } else {
                    newValue = value - Integer.parseInt(args[0].substring(1));
                }
            } else if (args[0].startsWith("+")) {
                if (args[0].length() == 1) {
                    newValue = value + 1;
                } else {
                    newValue = value + Integer.parseInt(args[0].substring(1));
                }
            } else {
                newValue = Integer.parseInt(args[0]);
            }

            if (newValue < 1) newValue = 1;
            if (newValue > 16) newValue = 16;

            Minecraft.getMinecraft().gameSettings.renderDistanceChunks = newValue;
            value = newValue;
        }

        sender.sendMessage(new TextComponentString("Rendering distance: " + value + " chunks"));
    }
}
