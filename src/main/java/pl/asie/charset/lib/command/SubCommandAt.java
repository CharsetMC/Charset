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
