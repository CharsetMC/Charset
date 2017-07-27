package pl.asie.charset.lib.command;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandCharset extends CommandBase {
    public static final Joiner COMMAS = Joiner.on(", ");
    public static final CommandCharset INSTANCE = new CommandCharset();
    static final List<SubCommand> SUB_COMMANDS = new ArrayList<>();
    static final Map<String, SubCommand> SUB_COMMAND_MAP = new HashMap<>();

    public static void register(SubCommand command) {
        if (SUB_COMMANDS.add(command)) {
            SUB_COMMAND_MAP.put(command.getName().toLowerCase(), command);
            for (String s : command.getAliases())
                SUB_COMMAND_MAP.put(s.toLowerCase(), command);
        }
    }

    private CommandCharset() {
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("ch");
    }

    @Override
    public String getName() {
        return "charset";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/charset [" + COMMAS.join(SUB_COMMANDS.stream()
                .filter(cmd -> sender.canUseCommand(cmd.getPermissionLevel(), "charset"))
                .filter(cmd -> (sender.getEntityWorld() != null && sender.getEntityWorld().isRemote) || cmd.getSide() == Side.SERVER)
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
                if (sender.canUseCommand(command.getPermissionLevel(), "charset")) {
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

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientChat(ClientChatEvent event) {
        if (event.getMessage().startsWith("/ch ") || event.getMessage().startsWith("/charset ")
                || "/ch".equals(event.getMessage()) || "/charset".equals(event.getMessage())) {
            ICommandSender sender = Minecraft.getMinecraft().player;
            String[] args0 = event.getMessage().split(" ");
            String[] args = new String[args0.length - 1];
            System.arraycopy(args0, 1, args, 0, args0.length - 1);

            if (args.length >= 1) {
                String[] args2 = new String[args.length - 1];
                System.arraycopy(args, 1, args2, 0, args.length - 1);

                SubCommand command = SUB_COMMAND_MAP.get(args[0].toLowerCase());
                if (command != null && (command.getSide() == Side.CLIENT || command.getName().equals("help"))) {
                    if (sender.canUseCommand(command.getPermissionLevel(), "charset")) {
                        event.setCanceled(true);
                        command.execute(Minecraft.getMinecraft().getIntegratedServer(), sender, args2);
                    } else {
                        sender.sendMessage(new TextComponentTranslation("commands.generic.permission").setStyle(new Style().setColor(TextFormatting.RED)));
                    }
                }
            } else {
                event.setCanceled(true);
                sender.sendMessage(new TextComponentString(getUsage(sender)));
            }
        }
    }
}
