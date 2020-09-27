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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import java.util.*;

public class SubCommandHand extends SubCommand {
    private final SubCommandAt parent;
    public SubCommandHand(SubCommandAt parent) {
        super("hand", Side.SERVER);
        this.parent = parent;
    }

    @Override
    public String getUsage() {
        return "Report information about the item in hand.";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args) {
        String[] newArgs = new String[args.length+1];
        newArgs[0] = "hand";
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return parent.getTabCompletions(server, sender, newArgs);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        String[] newArgs = new String[args.length+1];
        newArgs[0] = "hand";
        System.arraycopy(args, 0, newArgs, 1, args.length);
        parent.execute(server, sender, newArgs);
    }
}
