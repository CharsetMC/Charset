package pl.asie.charset.lib.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SubCommand {
    private final List<String> aliases = new ArrayList<>();
    private final String name;
    private final Side side;

    public SubCommand(String name, Side side) {
        this.name = name;
        this.side = side;
    }

    public SubCommand alias(String s) {
        aliases.add(s);
        return this;
    }

    public final Side getSide() {
        return side;
    }

    public final String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public int getPermissionLevel() {
        return 4;
    }

    public abstract String getUsage();

    public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args);
}
