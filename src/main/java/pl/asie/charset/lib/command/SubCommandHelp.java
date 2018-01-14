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

package pl.asie.charset.lib.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;

public class SubCommandHelp extends SubCommand {
    public SubCommandHelp(Side side) {
        super("help", side);
    }

    @Override
    public String getUsage() {
        return "Get help about a given command or all commands.";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length >= 1) {
            SubCommand command = (getSide() == Side.CLIENT ? CommandCharset.CLIENT : CommandCharset.SERVER).SUB_COMMAND_MAP.get(args[0].toLowerCase());
            if (command != null && sender.canUseCommand(getPermissionLevel(), "charset")
                    && (command.getSide() == Side.SERVER || (sender.getEntityWorld() != null && sender.getEntityWorld().isRemote))) {
                String[] usage = command.getUsage().split("\n");
                for (int i = 0; i < usage.length; i++) {
                    sender.sendMessage(new TextComponentString(usage[i]));
                }
            } else {
                sender.sendMessage(new TextComponentTranslation("commands.generic.parameter.invalid", args[0]).setStyle(new Style().setColor(TextFormatting.RED)));
            }
        } else {
            for (SubCommand command : (getSide() == Side.CLIENT ? CommandCharset.CLIENT : CommandCharset.SERVER).SUB_COMMANDS) {
                if (sender.canUseCommand(getPermissionLevel(), "charset")
                        && (command.getSide() == Side.SERVER || (sender.getEntityWorld() != null && sender.getEntityWorld().isRemote))) {
                    String[] usage = command.getUsage().split("\n");
                    if (usage.length > 0) {
                        String name = TextFormatting.BOLD + command.getName() + TextFormatting.RESET;
                        if (command.getAliases().size() > 0) {
                            name += " (" + CommandCharset.COMMAS.join(command.getAliases()) + ")";
                        }

                        sender.sendMessage(new TextComponentString("- " + name + ": " + usage[0]));
                        for (int i = 1; i < usage.length; i++) {
                            sender.sendMessage(new TextComponentString(usage[i]));
                        }
                    }
                }
            }
        }
    }
}
