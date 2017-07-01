package pl.asie.charset.lib.command;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class SubCommandClearChat extends SubCommand {
    public SubCommandClearChat() {
        super("clearchat", Side.CLIENT);
    }

    @Override
    public String getUsage() {
        return "Clear the chat.";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(true);
    }
}
