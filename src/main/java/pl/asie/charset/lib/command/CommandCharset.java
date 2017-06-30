package pl.asie.charset.lib.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

// TODO: make this good
public class CommandCharset extends CommandBase {
    @Override
    public String getName() {
        return "charset";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/charset hand";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            if ("hand".equalsIgnoreCase(args[0])) {
                Entity e = sender.getCommandSenderEntity();
                if (e instanceof EntityPlayer) {
                    ItemStack stack = ((EntityPlayer) e).getHeldItemMainhand();
                    sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + stack.toString()));
                    if (stack.hasTagCompound()) {
                        sender.sendMessage(new TextComponentString(stack.getTagCompound().toString()));
                    }
                }
            }
        }
    }
}
