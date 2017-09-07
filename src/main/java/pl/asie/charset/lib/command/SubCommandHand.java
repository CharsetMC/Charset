/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class SubCommandHand extends SubCommand {
    public SubCommandHand() {
        super("hand", Side.SERVER);
    }

    @Override
    public String getUsage() {
        return "Report information about the item in hand.\nParameters: stack (default), material, ore";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        Entity e = sender.getCommandSenderEntity();
        if (e instanceof EntityPlayer) {
            ItemStack stack = ((EntityPlayer) e).getHeldItemMainhand();

            if (stack.isEmpty()) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Empty hand!"));
                return;
            }

            if (args.length > 0 && "ore".equalsIgnoreCase(args[0])) {
                Collection<String> names = new ArrayList<>();
                for (int id : OreDictionary.getOreIDs(stack)) {
                    String name = OreDictionary.getOreName(id);
                    names.add(name);
                    sender.sendMessage(new TextComponentString("Ores: [" + CommandCharset.COMMAS.join(names) + "]"));
                }
            } else if (args.length > 0 && "material".equalsIgnoreCase(args[0])) {
                ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
                if (material == null) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not a material!"));
                } else {
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + material.getId()));
                    sender.sendMessage(new TextComponentString("[" + CommandCharset.COMMAS.join(material.getTypes()) + "]"));
                    for (Map.Entry<String, ItemMaterial> entry : material.getRelations().entrySet()) {
                        sender.sendMessage(new TextComponentString("-> " + entry.getKey() + ": " + entry.getValue().getId()));
                    }
                }
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + stack.toString() + " " + TextFormatting.GRAY + "(" + stack.getItem().getRegistryName() + ")"));
                if (stack.hasTagCompound()) {
                    sender.sendMessage(new TextComponentString(stack.getTagCompound().toString()));
                }
            }
        }
    }
}
