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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opencl.CL;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CommandCharset extends CommandBase {
    public static final Joiner COMMAS = Joiner.on(", ");
    public static final CommandCharset CLIENT = new CommandCharset(Side.CLIENT);
    public static final CommandCharset SERVER = new CommandCharset(Side.SERVER);
    final List<SubCommand> SUB_COMMANDS = new ArrayList<>();
    final Map<String, SubCommand> SUB_COMMAND_MAP = new HashMap<>();
    final Side side;

    private void registerInternal(SubCommand command) {
        if (SUB_COMMANDS.add(command)) {
            SUB_COMMAND_MAP.put(command.getName().toLowerCase(), command);
            for (String s : command.getAliases())
                SUB_COMMAND_MAP.put(s.toLowerCase(), command);
        }
    }

    public static void register(SubCommand command) {
         if (command.getSide() == Side.CLIENT) {
             CLIENT.registerInternal(command);
         } else {
             SERVER.registerInternal(command);
         }
    }

    private CommandCharset(Side side) {
        this.side = side;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>();
            for (Map.Entry<String, SubCommand> cmdEntry : SUB_COMMAND_MAP.entrySet()) {
                if (sender.canUseCommand(cmdEntry.getValue().getPermissionLevel(), getName())) {
                    cmds.add(cmdEntry.getKey());
                }
            }

            return getListOfStringsMatchingLastWord(args, cmds);
        } else if (args.length > 1) {
            SubCommand command = SUB_COMMAND_MAP.get(args[0].toLowerCase());
            if (command != null) {
                String[] args2 = new String[args.length - 1];
                System.arraycopy(args, 1, args2, 0, args.length - 1);

                return command.getTabCompletions(server, sender, args2);
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of(side == Side.CLIENT ? "chc" : "ch");
    }

    @Override
    public String getName() {
        return side == Side.CLIENT ? "charsetc" : "charset";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/"+getName()+" [" + COMMAS.join(SUB_COMMANDS.stream()
                .filter(cmd -> sender.canUseCommand(cmd.getPermissionLevel(), "charset"))
                .filter(cmd -> cmd.getSide() == side)
                .map(SubCommand::getName)
                .collect(Collectors.toList())) + "]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args.length - 1);

            SubCommand command = SUB_COMMAND_MAP.get(args[0].toLowerCase());
            if (command != null) {
                if (sender.canUseCommand(command.getPermissionLevel(), getName())) {
                    command.execute(server, sender, args2);
                } else {
                    sender.sendMessage(new TextComponentTranslation("commands.generic.permission").setStyle(new Style().setColor(TextFormatting.RED)));
                }
            } else {
                sender.sendMessage(new TextComponentTranslation("commands.generic.parameter.invalid", args[0]).setStyle(new Style().setColor(TextFormatting.RED)));
            }
        } else {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
        }
    }
}
