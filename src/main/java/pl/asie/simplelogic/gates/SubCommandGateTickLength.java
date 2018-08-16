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
