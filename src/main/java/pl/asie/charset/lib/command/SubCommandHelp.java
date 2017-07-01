package pl.asie.charset.lib.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class SubCommandHelp extends SubCommand {
    public SubCommandHelp() {
        super("help", Side.SERVER);
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
            SubCommand command = CommandCharset.SUB_COMMAND_MAP.get(args[0].toLowerCase());
            if (command != null && sender.canUseCommand(getPermissionLevel(), "charset")
                    && (command.getSide() == Side.SERVER || (sender.getEntityWorld() != null && sender.getEntityWorld().isRemote))) {
                String[] usage = command.getUsage().split("\n");
                for (int i = 0; i < usage.length; i++) {
                    sender.sendMessage(new TextComponentString(usage[i]));
                }
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Command not found!"));
            }
        } else {
            for (SubCommand command : CommandCharset.SUB_COMMANDS) {
                if (sender.canUseCommand(getPermissionLevel(), "charset")
                        && (command.getSide() == Side.SERVER || (sender.getEntityWorld() != null && sender.getEntityWorld().isRemote))) {
                    String[] usage = command.getUsage().split("\n");
                    if (usage.length > 0) {
                        String name = TextFormatting.BOLD + command.getName() + TextFormatting.RESET;
                        if (getAliases().size() > 0) {
                            name += " (" + CommandCharset.COMMAS.join(getAliases()) + ")";
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
