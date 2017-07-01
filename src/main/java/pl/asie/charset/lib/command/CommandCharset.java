package pl.asie.charset.lib.command;

import com.google.common.base.Joiner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CommandCharset extends CommandBase {
    private static final Joiner COMMAS = Joiner.on(", ");
    @Override
    public String getName() {
        return "charset";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/charset <hand [material|ore]>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            if ("hand".equalsIgnoreCase(args[0])) {
                Entity e = sender.getCommandSenderEntity();
                if (e instanceof EntityPlayer) {
                    ItemStack stack = ((EntityPlayer) e).getHeldItemMainhand();

                    if (stack.isEmpty()) {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Empty hand!"));
                        return;
                    }

                    if (args.length >= 2 && "ore".equalsIgnoreCase(args[1])) {
                        Collection<String> names = new ArrayList<>();
                        for (int id : OreDictionary.getOreIDs(stack)) {
                            String name = OreDictionary.getOreName(id);
                            names.add(name);
                            sender.sendMessage(new TextComponentString("Ores: [" + COMMAS.join(names) + "]"));
                        }
                    } else if (args.length >= 2 && "material".equalsIgnoreCase(args[1])) {
                        ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
                        if (material == null) {
                            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not a material!"));
                        } else {
                            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + material.getId()));
                            sender.sendMessage(new TextComponentString("[" + COMMAS.join(material.getTypes()) + "]"));
                            for (Map.Entry<String, ItemMaterial> entry : material.getRelations().entrySet()) {
                                sender.sendMessage(new TextComponentString("-> " + entry.getKey() + ": " + entry.getValue().getId()));
                            }
                        }
                    } else {
                        sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + stack.toString()));
                        if (stack.hasTagCompound()) {
                            sender.sendMessage(new TextComponentString(stack.getTagCompound().toString()));
                        }
                    }
                }
            }
        }
    }
}
