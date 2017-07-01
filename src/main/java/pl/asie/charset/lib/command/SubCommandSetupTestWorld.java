package pl.asie.charset.lib.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
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
