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

package pl.asie.charset.module.laser.system;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.command.SubCommand;
import pl.asie.charset.module.laser.CharsetLaser;

public class SubCommandDebugLasersClient extends SubCommand {
	public static boolean enabled = false;

	public SubCommandDebugLasersClient() {
		super("debugLasersClient", Side.CLIENT);
	}

	@Override
	public String getUsage() {
		return "Toggle client-side laser debugging information.";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		enabled = !enabled;
		if (enabled) {
			sender.sendMessage(new TextComponentString("Client-side laser debugging enabled."));
		} else {
			sender.sendMessage(new TextComponentString("Client-side laser debugging disabled."));
		}
	}
}
