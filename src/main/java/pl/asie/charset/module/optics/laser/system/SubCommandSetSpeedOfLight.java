/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.optics.laser.system;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.command.SubCommand;

public class SubCommandSetSpeedOfLight extends SubCommand {
	public SubCommandSetSpeedOfLight() {
		super("setSpeedOfLight", Side.SERVER);
	}

	@Override
	public String getUsage() {
		return "Sets speed of laser light, in ticks. 0 - instant";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length >= 1) {
			int value = new Integer(args[0]);
			if (value > 0) {
				LaserWorldStorageServer.IS_LAZY = true;
				LaserWorldStorageServer.LAZY_LIGHT_DELAY = value - 1;
				sender.sendMessage(new TextComponentString("Set speed of light to " + value + " ticks."));
			} else {
				LaserWorldStorageServer.IS_LAZY = false;
				sender.sendMessage(new TextComponentString("Set speed of light to instant (default)."));
			}
			sender.sendMessage(new TextComponentString("(Please use this command only for debugging or as a last resort. This is not a config option.)"));
		} else {
			sender.sendMessage(new TextComponentString("No value provided!"));
		}
	}
}
