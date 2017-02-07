/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.carts;

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
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.carts.link.Linkable;
import pl.asie.charset.carts.link.TrainLinker;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.ModCharsetBase;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = ModCharsetCarts.MODID, name = ModCharsetCarts.NAME, version = ModCharsetCarts.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetCarts extends ModCharsetBase {
	public static final Map<Class<? extends Entity>, Class<? extends EntityMinecart>> REPLACEMENT_MAP = new HashMap<>();
	public static final String MODID = "charsetcarts";
	public static final String NAME = "∐";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.carts.ProxyClient", serverSide = "pl.asie.charset.carts.ProxyCommon")
	public static ProxyCommon proxy;

	public static final ResourceLocation LINKABLE_LOC = new ResourceLocation("charsetcarts:linkable");
	@CapabilityInject(Linkable.class)
	public static Capability<Linkable> LINKABLE;

	@Mod.Instance(MODID)
	public static ModCharsetCarts instance;
	public static TrackCombiner combiner;
	public static TrainLinker linker;
	public static int minecartStackSize;

	public static BlockRailCharset blockRailCross;
	public static Item itemLinker;

	private void register(Class<? extends EntityMinecart> minecart, String name) {
		EntityRegistry.registerModEntity(new ResourceLocation("charsetcarts:" + name), minecart, "charsetcarts:" + name, 2, this, 64, 1, true);
	}

	private void register(Class<? extends EntityMinecart> minecart, String name, Class<? extends Entity> from) {
		register(minecart, name);
		REPLACEMENT_MAP.put(from, minecart);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		minecartStackSize = config.getInt("minecartStackSize", "tweaks", 4, 1, 64, "Sets the minimum stack size for all minecarts.");

		ModCharsetLib.proxy.registerBlock(blockRailCross = new BlockRailCharset(), "rail_charset");
		ModCharsetLib.proxy.registerItemModel(blockRailCross, 0, "charsetcarts:rail_charset");

		MinecraftForge.EVENT_BUS.register(proxy);
		CapabilityManager.INSTANCE.register(Linkable.class, Linkable.PROVIDER.getStorage(), Linkable.class);

		if (ModCharsetLib.INDEV) {
			linker = new TrainLinker();
			MinecraftForge.EVENT_BUS.register(linker);

			itemLinker = new Item().setCreativeTab(ModCharsetLib.CREATIVE_TAB).setUnlocalizedName("linker").setMaxStackSize(1);
			GameRegistry.register(itemLinker.setRegistryName("linker"));
		}

		if (config.getBoolean("trackCombiner", "features", true, "Enables the Track Combiner, replacing the usual way of crafting expansion rails with an in-world mechanism.")) {
			combiner = new TrackCombiner();
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
		if (ModCharsetLib.INDEV) {
			register(EntityMinecartImproved.class, "rminecart", EntityMinecart.class);
		}

		MinecraftForge.EVENT_BUS.register(this);

		if (combiner != null) {
			MinecraftForge.EVENT_BUS.register(combiner);

			// TODO: This needs a redesign... Possibly move the Combiner to Lib.
			combiner.register(Blocks.RAIL, blockRailCross.getDefaultState(), new ItemStack(Blocks.RAIL));
			combiner.register(Blocks.RAIL, blockRailCross.getDefaultState().withProperty(BlockRailCharset.DIRECTION, BlockRailBase.EnumRailDirection.EAST_WEST), new ItemStack(Blocks.RAIL));
			registerCombinerRecipeForDirs(Blocks.RAIL, BlockRail.SHAPE, Blocks.DETECTOR_RAIL, BlockRailDetector.SHAPE, new ItemStack(Blocks.STONE_PRESSURE_PLATE));
		} else {
			GameRegistry.addShapedRecipe(new ItemStack(blockRailCross, 2), " r ", "r r", " r ", 'r', new ItemStack(Blocks.RAIL));
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (Item item : Item.REGISTRY) {
			if (item instanceof ItemMinecart && item.getItemStackLimit() < minecartStackSize) {
				item.setMaxStackSize(minecartStackSize);
			}
		}
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
				EntityMinecart minecart = REPLACEMENT_MAP.get(classy).getConstructor(World.class, double.class, double.class, double.class).newInstance(
						event.getWorld(), event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ
				);
				event.getWorld().spawnEntity(minecart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
