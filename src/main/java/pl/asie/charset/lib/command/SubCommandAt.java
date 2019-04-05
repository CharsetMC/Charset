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

package pl.asie.charset.lib.command;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.lib.utils.MethodHandleHelper;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.lib.utils.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SubCommandAt extends SubCommand {
    private static final String END_KEY = "[[END]]";

    private static class Dummy {
        static final Dummy INSTANCE = new Dummy();

        private Dummy() {

        }
    }

    private static class Context {
        private final MinecraftServer server;
        private final ICommandSender sender;
        private final String[] args;
        private int pos;

        public Context(MinecraftServer server, ICommandSender sender, String[] args) {
            this.server = server;
            this.sender = sender;
            this.args = args;
            this.pos = 0;
        }

        @Nullable
        public String pop() {
            return pos >= args.length ? END_KEY : args[pos++];
        }
    }

    private final Table<Class, String, BiFunction<Context, Object, Object>> transformationTable = HashBasedTable.create();

    private static Object printClasses(Context ctx, Object o) {
        ctx.sender.sendMessage(new TextComponentString("Classes:"));
        for (Class cl : MethodHandleHelper.classes(o.getClass())) {
            String name = cl.getName();

            if (cl.isInterface()) {
                name = TextFormatting.AQUA + name;
            }

            ctx.sender.sendMessage(new TextComponentString("- " + name));
        }
        return null;
    }

    public SubCommandAt() {
        super("at", Side.SERVER);

        transformationTable.put(Dummy.class, "hand", (ctx, a) -> {
            Entity e = ctx.sender.getCommandSenderEntity();
            if (e instanceof EntityLivingBase) {
                ItemStack stack = ((EntityLivingBase) e).getHeldItemMainhand();

                if (stack.isEmpty()) {
                    ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "Empty hand!"));
                    return null;
                }

                return stack;
            }

            ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not a valid hand holder!"));
            return null;
        });

        transformationTable.put(Dummy.class, "block", (ctx, a) -> {
            Entity e = ctx.sender.getCommandSenderEntity();
            if (e instanceof EntityLivingBase) {
                RayTraceResult result = e.getEntityWorld().rayTraceBlocks(RayTraceUtils.getStart((EntityLivingBase) e), RayTraceUtils.getEnd((EntityLivingBase) e), false);
                if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                    return result.getBlockPos();
                }
            }

            ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "No block found!"));
            return null;
        });

        transformationTable.put(Dummy.class, "part", (ctx, a) -> {
            // TODO: This is ugly...

            Entity e = ctx.sender.getCommandSenderEntity();
            if (e instanceof EntityLivingBase) {
                RayTraceResult result = e.getEntityWorld().rayTraceBlocks(RayTraceUtils.getStart((EntityLivingBase) e), RayTraceUtils.getEnd((EntityLivingBase) e), false);
                if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                    MultipartUtils.ExtendedRayTraceResult trueResult = MultipartUtils.INSTANCE.getTrueResult(result);
                    return trueResult.getTile(ctx.sender.getEntityWorld());
                }
            }

            ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "No tile found!"));
            return null;
        });

        transformationTable.put(Dummy.class, "self", (ctx, a) -> {
            Entity e = ctx.sender.getCommandSenderEntity();
            if (e instanceof EntityLivingBase) {
                return e;
            }

            ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "No self entity found!"));
            return null;
        });

        transformationTable.put(ItemStack.class, "material", (ctx, a) -> {
            ItemStack stack = (ItemStack) a;

            ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
            if (material == null) {
                ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not a material!"));
            } else {
                ctx.sender.sendMessage(new TextComponentString("Material ID: " + TextFormatting.GREEN + material.getId()));
                ctx.sender.sendMessage(new TextComponentString("Material types: [" + CommandCharset.COMMAS.join(material.getTypes()) + "]"));
                for (Map.Entry<String, ItemMaterial> entry : material.getRelations().entrySet()) {
                    ctx.sender.sendMessage(new TextComponentString("-> " + entry.getKey() + ": " + entry.getValue().getId()));
                }
            }

            return null;
        });

        transformationTable.put(ItemStack.class, "ore", (ctx, a) -> {
            ItemStack stack = (ItemStack) a;

            Collection<String> names = new ArrayList<>();
            for (int id : OreDictionary.getOreIDs(stack)) {
                String name = OreDictionary.getOreName(id);
                names.add(name);
            }
            ctx.sender.sendMessage(new TextComponentString("Ores: [" + CommandCharset.COMMAS.join(names) + "]"));

            return null;
        });

	    transformationTable.put(BlockPos.class, "tile", (ctx, a) -> {
		    BlockPos pos = (BlockPos) a;
		    return ctx.sender.getEntityWorld().getTileEntity(pos);
	    });

	    transformationTable.put(ItemStack.class, "tag", (ctx, a) -> ((ItemStack) a).hasTagCompound() ? ((ItemStack) a).getTagCompound() : new NBTTagCompound());

        transformationTable.put(Entity.class, "tag", (ctx, a) -> {
            NBTTagCompound tag = new NBTTagCompound();
            ((Entity) a).writeToNBT(tag);
            return tag;
        });

	    transformationTable.put(TileEntity.class, "tag", (ctx, a) -> {
		    NBTTagCompound tag = new NBTTagCompound();
		    ((TileEntity) a).writeToNBT(tag);
		    return tag;
	    });

	    transformationTable.put(NBTBase.class, END_KEY, (ctx, a) -> {
		    ctx.sender.sendMessage(new TextComponentString("Tag: " + a));
		    return null;
	    });

	    transformationTable.put(BlockPos.class, END_KEY, (ctx, a) -> {
	    	BlockPos pos = (BlockPos) a;
	    	IBlockState state = ctx.sender.getEntityWorld().getBlockState(pos);

	    	ctx.sender.sendMessage(new TextComponentString("Block at " + pos + ": " + state.getBlock().getRegistryName()));
		    for (IProperty property : state.getPropertyKeys()) {
			    //noinspection unchecked
			    ctx.sender.sendMessage(new TextComponentString("- " + property.getName() + " = " + state.getValue(property)));
		    }

		    return null;
	    });

	    transformationTable.put(TileEntity.class, END_KEY, (ctx, a) -> {
	    	TileEntity tile = (TileEntity) a;
		    ctx.sender.sendMessage(new TextComponentString("Tile at " + tile.getPos() + ": " + TileEntity.getKey(tile.getClass())));
		    return null;
	    });

	    transformationTable.put(Entity.class, END_KEY, (ctx, a) -> {
		    Entity entity = (Entity) a;
		    ctx.sender.sendMessage(new TextComponentString("Entity at " + entity.getPositionVector() + ": " + entity.getName()));
		    return null;
	    });

        transformationTable.put(Entity.class, "item", (ctx, a) -> {
            Entity entity = (Entity) a;
            if (entity instanceof EntityItemFrame) {
                return ((EntityItemFrame) entity).getDisplayedItem();
            } else {
                ctx.sender.sendMessage(new TextComponentString(TextFormatting.RED + "No item found!"));
                return null;
            }
        });

        transformationTable.put(ItemStack.class, END_KEY, (ctx, a) -> {
            ItemStack stack = (ItemStack) a;
            ctx.sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + stack.toString() + " " + TextFormatting.GRAY + "(" + stack.getItem().getRegistryName() + ")"));
            ctx.sender.sendMessage(new TextComponentString("- max stack size = " + stack.getMaxStackSize()));
            if (stack.isItemStackDamageable()) {
                ctx.sender.sendMessage(new TextComponentString("- max damage = " + stack.getMaxDamage()));
            }

            return null;
        });

        if (CharsetLib.showHandClasses) {
            transformationTable.put(ItemStack.class, "class", (ctx, a) -> printClasses(ctx, ((ItemStack) a).getItem()));
            transformationTable.put(BlockPos.class, "class", (ctx, a) -> printClasses(ctx, ctx.sender.getEntityWorld().getBlockState((BlockPos) a).getBlock()));
            transformationTable.put(Entity.class, "class", SubCommandAt::printClasses);
            transformationTable.put(TileEntity.class, "class", SubCommandAt::printClasses);
        }
    }

    @Override
    public String getUsage() {
        return "Report information about the object being pointed at.";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args) {
        Context context = new Context(server, sender, args);
        Object o = Dummy.INSTANCE;

        while (o != null && context.pos < args.length - 1) {
            boolean found = false;
            String s = context.pop();
            for (Class cl : MethodHandleHelper.classes(o.getClass())) {
                BiFunction<Context, Object, Object> func = transformationTable.get(cl, s);
                if (func != null) {
                    o = func.apply(context, o);
                    found = true;
                    break;
                }
            }

            if (!found) {
                return Collections.emptyList();
            }
        }

        if (o == null) {
            return Collections.emptyList();
        }

        String prefix = args[args.length - 1].toLowerCase();
        Set<String> strs = new TreeSet<>();

        for (Class cl : MethodHandleHelper.classes(o.getClass())) {
            for (String s : transformationTable.row(cl).keySet()) {
                if (!s.equals(END_KEY) && s.toLowerCase().startsWith(prefix)) {
                    strs.add(s);
                }
            }
        }

        return Lists.newArrayList(strs);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        Context context = new Context(server, sender, args);
        Object o = Dummy.INSTANCE;

        while (o != null) {
            boolean found = false;
            String s = context.pop();
            for (Class cl : MethodHandleHelper.classes(o.getClass())) {
                BiFunction<Context, Object, Object> func = transformationTable.get(cl, s);
                if (func != null) {
                    o = func.apply(context, o);
                    found = true;
                    break;
                }
            }

            if (!found) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid command!"));
                return;
            }
        }
    }
}
