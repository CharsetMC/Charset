package pl.asie.charset.lib.command;

import mcmultipart.api.slot.IPartSlot;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;

public class SubCommandClientCmdList extends SubCommand {
    private final String usage;
    private final String[] cmds;

    public SubCommandClientCmdList(String name, String usage, String... cmds) {
        super(name, Side.CLIENT);
        this.usage = usage;
        this.cmds = cmds;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerSP) {
            for (String m : cmds)
                ((EntityPlayerSP) sender).sendChatMessage(m);
        }
    }
}
