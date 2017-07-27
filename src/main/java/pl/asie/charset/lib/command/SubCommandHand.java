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
