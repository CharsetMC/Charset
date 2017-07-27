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
