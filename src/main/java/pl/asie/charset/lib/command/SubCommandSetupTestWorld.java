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

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class SubCommandSetupTestWorld extends SubCommand {
    public SubCommandSetupTestWorld() {
        super("setuptestworld", Side.CLIENT);
    }

    @Override
    public String getUsage() {
        return "Set up development environment test world settings.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        ((EntityPlayerSP) sender).sendChatMessage("/gamerule doDaylightCycle false");
        ((EntityPlayerSP) sender).sendChatMessage("/gamerule doMobSpawning false");
        ((EntityPlayerSP) sender).sendChatMessage("/gamerule keepInventory true");
        ((EntityPlayerSP) sender).sendChatMessage("/weather clear 999999");
        ((EntityPlayerSP) sender).sendChatMessage("/time set 1200");

        for (World world : server.worlds) {
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityLiving || entity instanceof EntityItem) {
                    entity.setDead();
                }
            }
        }
    }
}
