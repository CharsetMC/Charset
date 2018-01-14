/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.transport.carts;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.transport.carts.link.Linkable;
import pl.asie.charset.module.transport.carts.link.TrainLinker;

import java.util.HashMap;
import java.util.Map;

@CharsetModule(
        name = "transport.carts",
        description = "Minecart rework. WIP",
        profile = ModuleProfile.INDEV
)
public class CharsetTransportCarts {
    public static final Map<Class<? extends Entity>, Class<? extends EntityMinecart>> REPLACEMENT_MAP = new HashMap<>();
    public static final ResourceLocation LINKABLE_LOC = new ResourceLocation("charsetcarts:linkable");
    @CapabilityInject(Linkable.class)
    public static Capability<Linkable> LINKABLE;

    @CharsetModule.Instance
    public static CharsetTransportCarts instance;

    public static TrackCombiner combiner;
    public static TrainLinker linker;

    public static Item itemLinker;

    private void register(Class<? extends EntityMinecart> minecart, String name) {
        RegistryUtils.register(minecart, name, 64, 1, true);
    }

    private void register(Class<? extends EntityMinecart> minecart, String name, Class<? extends Entity> from) {
        register(minecart, name);
        REPLACEMENT_MAP.put(from, minecart);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(Linkable.class, Linkable.STORAGE, Linkable::new);

        if (ModCharset.profile.includes(ModuleProfile.INDEV)) {
            linker = new TrainLinker();
            MinecraftForge.EVENT_BUS.register(linker);

            itemLinker = new Item().setCreativeTab(ModCharset.CREATIVE_TAB).setUnlocalizedName("linker").setMaxStackSize(1);
        }

        combiner = new TrackCombiner();
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        if (ModCharset.profile.includes(ModuleProfile.INDEV)) {
            RegistryUtils.register(event.getRegistry(), itemLinker, "linker");
        }
    }

    private void registerCombinerRecipeForDirs(Block railSrc, IProperty<BlockRailBase.EnumRailDirection> propSrc, Block railDst, IProperty<BlockRailBase.EnumRailDirection> propDst, ItemStack with) {
        for (BlockRailBase.EnumRailDirection direction : propSrc.getAllowedValues()) {
            if (propDst.getAllowedValues().contains(direction)) {
                combiner.register(railSrc.getDefaultState().withProperty(propSrc, direction),
                        railDst.getDefaultState().withProperty(propDst, direction),
                        with);
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (ModCharset.profile.includes(ModuleProfile.INDEV)) {
            register(EntityMinecartImproved.class, "rminecart", EntityMinecart.class);
        }

        if (combiner != null) {
            MinecraftForge.EVENT_BUS.register(combiner);
            registerCombinerRecipeForDirs(Blocks.RAIL, BlockRail.SHAPE, Blocks.DETECTOR_RAIL, BlockRailDetector.SHAPE, new ItemStack(Blocks.STONE_PRESSURE_PLATE));
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        /* for (Item item : Item.REGISTRY) {
            if (item instanceof ItemMinecart && item.getItemStackLimit() < minecartStackSize) {
                item.setMaxStackSize(minecartStackSize);
            }
        } */
    }

    private final Map<EntityPlayer, EntityMinecart> linkMap = new HashMap<>();

    @SubscribeEvent
    public void onNothingInteract(PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getEntityPlayer().getEntityWorld().isRemote
                && event.getItemStack().getItem() == itemLinker) {
            if (linkMap.containsKey(event.getEntityPlayer())) {
                linkMap.remove(event.getEntityPlayer());
                event.getEntityPlayer().sendMessage(new TextComponentString("dev_unlinked"));
                event.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityMinecart
                && !event.getTarget().getEntityWorld().isRemote
                && event.getItemStack().getItem() == itemLinker) {
            EntityMinecart cart = (EntityMinecart) event.getTarget();

            if (linkMap.containsKey(event.getEntityPlayer())) {
                EntityMinecart cartOther = linkMap.remove(event.getEntityPlayer());
                Linkable link = linker.get(cart);
                Linkable linkOther = linker.get(cartOther);
                if (event.getEntityPlayer().isSneaking()) {
                    if (linker.unlink(link, linkOther)) {
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_unlinked2"));
                    } else {
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_unlink2_failed"));
                    }
                } else {
                    if (link.next == null && linkOther.previous == null) {
                        linker.link(link, linkOther);
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_linked2"));
                    } else if (link.previous == null && linkOther.next == null) {
                        linker.link(linkOther, link);
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_linked2"));
                    } else {
                        event.getEntityPlayer().sendMessage(new TextComponentString("dev_link2_failed"));
                    }
                }
            } else {
                linkMap.put(event.getEntityPlayer(), cart);
                event.getEntityPlayer().sendMessage(new TextComponentString("dev_linked1"));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        Class<? extends Entity> classy = event.getEntity().getClass();
        if (REPLACEMENT_MAP.containsKey(classy)) {
            try {
                event.setCanceled(true);
                EntityMinecart painting = REPLACEMENT_MAP.get(classy).getConstructor(World.class, double.class, double.class, double.class).newInstance(
                        event.getWorld(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ
                );
                event.getWorld().spawnEntity(painting);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
