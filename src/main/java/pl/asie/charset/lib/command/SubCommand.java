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

package pl.asie.charset.lib.command;

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

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    public abstract String getUsage();

    public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args);
}
