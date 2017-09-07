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

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.utils.RayTraceUtils;

public class SubCommandAt extends SubCommand {
    public SubCommandAt() {
        super("at", Side.SERVER);
    }

    @Override
    public String getUsage() {
        return "Report information about the block being pointed at.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        Entity e = sender.getCommandSenderEntity();
        if (e instanceof EntityPlayer) {
            RayTraceResult result = e.getEntityWorld().rayTraceBlocks(RayTraceUtils.getStart((EntityPlayer) e), RayTraceUtils.getEnd((EntityPlayer) e), true);
            if (result == null) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Nothing found!"));
            } else {
                switch (result.typeOfHit) {
                    case MISS:
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Nothing found!"));
                        break;
                    case BLOCK:
                        IBlockState state = e.getEntityWorld().getBlockState(result.getBlockPos());
                        sender.sendMessage(new TextComponentString(state.toString()));
                        if (state.getBlock().hasTileEntity(state)) {
                            TileEntity tile = e.getEntityWorld().getTileEntity(result.getBlockPos());
                            if (tile != null) {
                                sender.sendMessage(new TextComponentString("- Entity: " + TileEntity.getKey(tile.getClass())));
                            }
                        }
                        break;
                    case ENTITY:
                        sender.sendMessage(new TextComponentString(result.entityHit.toString()));
                        break;
                }
            }
        }
    }
}
