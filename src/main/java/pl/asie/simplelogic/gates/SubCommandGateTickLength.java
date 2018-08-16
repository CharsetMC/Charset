package pl.asie.simplelogic.gates;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.command.SubCommand;

public class SubCommandGateTickLength extends SubCommand {
	public SubCommandGateTickLength() {
		super("gateTickLength", Side.SERVER);
	}

	@Override
	public String getUsage() {
		return "Get/set gate tick length, in in-game ticks.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length > 0) {
			try {
				SimpleLogicGates.redstoneTickLength = MathHelper.clamp(Integer.parseInt(args[0]), 1, 20);
				sender.sendMessage(new TextComponentString("Set gate tick length to " + SimpleLogicGates.redstoneTickLength));
			} catch (NumberFormatException e) {
				sender.sendMessage(new TextComponentString("Invalid number: " + args[0]));
			}
		} else {
			sender.sendMessage(new TextComponentString("Gate tick length = " + SimpleLogicGates.redstoneTickLength));
		}
	}
}
