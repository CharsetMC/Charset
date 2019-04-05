/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.notify;

import com.google.common.base.Joiner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import pl.asie.charset.lib.notify.component.NotificationComponentString;

import java.util.EnumSet;

// TODO: Restore --show-item
public class CommandMutter extends CommandBase {
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof TileEntity || sender instanceof Entity)) {
            return;
        }
        EnumSet theStyle = EnumSet.noneOf(NoticeStyle.class);
        ItemStack heldItem = ItemStack.EMPTY;
        if (sender instanceof EntityLivingBase) {
            heldItem = ((EntityLivingBase) sender).getHeldItem(EnumHand.MAIN_HAND);
        }
        ItemStack sendItem = ItemStack.EMPTY;
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equalsIgnoreCase("--long")) {
                theStyle.add(NoticeStyle.LONG);
            } else if (s.equalsIgnoreCase("--show-item") && !heldItem.isEmpty()) {
                theStyle.add(NoticeStyle.DRAWITEM);
                sendItem = heldItem;
            } else {
                break;
            }
            args[i] = null;
        }
        String msg = Joiner.on(" ").skipNulls().join(args);
        msg = msg.replace("\\n", "\n");
        new Notice(sender, NotificationComponentString.raw(msg)).withStyle(theStyle)/* .withItem(sendItem)*/.sendToAll();
    }
    
    @Override
    public String getUsage(ICommandSender icommandsender) {
        return "/mutter [--long] [--show-item] some text. Clears if empty";
    }
    
    @Override
    public String getName() {
        return "mutter";
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof Entity;
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public int compareTo(ICommand otherCmd) {
        return this.getName().compareTo(otherCmd.getName());
    }
}
