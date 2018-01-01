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

package pl.asie.charset.module.storage.barrels;

import com.google.common.collect.Lists;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.render.ArrowHighlightHandler;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.*;

@CharsetModule(
		name = "storage.barrels",
		description = "Simple barrels",
		profile = ModuleProfile.STABLE
)
public class CharsetStorageBarrels {
	public static final List<ItemStack> CREATIVE_BARRELS = Lists.newArrayList();
	public static List<ItemStack> BARRELS = Collections.emptyList();
	public static List<ItemStack> BARRELS_NORMAL = Lists.newArrayList();

	public static TObjectIntMap<Item> stackDivisorMultiplierMap = new TObjectIntHashMap<>();
	public static TObjectIntMap<Item> stackSizeMultiplierMap = new TObjectIntHashMap<>();

	@CharsetModule.Instance
	public static CharsetStorageBarrels instance;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	@CharsetModule.Configuration
	public static Configuration config;

	public static BlockBarrel barrelBlock;
	public static ItemDayBarrel barrelItem;
	public static ItemMinecartDayBarrel barrelCartItem;

	public static boolean renderBarrelText, renderBarrelItem, renderBarrelItem3D;
	public static boolean enableSilkyBarrels, enableStickyBarrels, enableHoppingBarrels;
	public static int maxDroppedStacks;

	public static boolean isEnabled(BarrelUpgrade upgrade) {
		if (upgrade == BarrelUpgrade.SILKY) {
			return enableSilkyBarrels;
		} else if (upgrade == BarrelUpgrade.HOPPING) {
			return enableHoppingBarrels;
		} else if (upgrade == BarrelUpgrade.STICKY) {
			return enableStickyBarrels;
		} else {
			return true;
		}
	}

	@Mod.EventHandler
	public void reloadReentrantConfig(CharsetLoadConfigEvent event) {
		enableSilkyBarrels = ConfigUtils.getBoolean(config, "features", "enableSilkyBarrels", !ModCharset.isModuleLoaded("tweak.blockCarrying"), "Enable silky barrels. On by default unless tweak.blockCarrying is also present.", true);
		enableHoppingBarrels = ConfigUtils.getBoolean(config, "features", "enableHoppingBarrels", true, "Enable hopping barrels. On by default.", true);
		enableStickyBarrels = ConfigUtils.getBoolean(config, "features", "enableStickyBarrels", true, "Enable sticky barrels. On by default.", true);
		maxDroppedStacks = ConfigUtils.getInt(config, "general", "maxDroppedStacks", 1024, 0, (Integer.MAX_VALUE / 64), "The maximum amount of stacks to be dropped when a barrel is broken.", true);
		renderBarrelItem3D = ConfigUtils.getBoolean(config, "render", "renderItem3D", false, "Should items use fancy 3D rendering?", false);
		renderBarrelItem = ConfigUtils.getBoolean(config, "render", "renderItem", true, "Should items be rendered on barrels?", false);
		renderBarrelText = ConfigUtils.getBoolean(config, "render", "renderText", true, "Should text be rendered on barrels?", false);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		barrelBlock = new BlockBarrel();
		barrelItem = new ItemDayBarrel(barrelBlock);
		barrelCartItem = new ItemMinecartDayBarrel();

		barrelBlock.setHarvestLevel("axe", 0);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(barrelItem, 0, "charset:barrel");
		RegistryUtils.registerModel(barrelCartItem, 0, "charset:barrelCart");
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), barrelBlock, "barrel");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), barrelItem, "barrel");
		RegistryUtils.register(event.getRegistry(), barrelCartItem, "barrelCart");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerTileEntity(TileEntityDayBarrel.class, "charset:barrel");
		RegistryUtils.register(EntityMinecartDayBarrel.class, "barrelCart", 64, 1, true);

		packet.registerPacket(0x01, PacketBarrelCountUpdate.class);
		FMLInterModComms.sendMessage("charset", "addCarry", barrelBlock.getRegistryName());
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		CREATIVE_BARRELS.add(TileEntityDayBarrel.makeBarrel(
				EnumSet.of(BarrelUpgrade.HOPPING, BarrelUpgrade.INFINITE),
				ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.BEDROCK)),
				ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.DIAMOND_BLOCK))
		));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		populateBarrelStackLists();
	}

	private void populateBarrelStackLists() {
		BARRELS = barrelBlock.getSubItemProvider().getAllItems();

		BARRELS_NORMAL.clear();
		for (ItemStack is : BARRELS) {
			Set<BarrelUpgrade> upgradeSet = EnumSet.noneOf(BarrelUpgrade.class);
			TileEntityDayBarrel.populateUpgrades(upgradeSet, is.getTagCompound());
			if (upgradeSet.isEmpty()) {
				BARRELS_NORMAL.add(is);
			}
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartDayBarrel.class, RenderMinecartDayBarrel::new);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDayBarrel.class, new TileEntityDayBarrelRenderer());
		ArrowHighlightHandler.register(barrelItem);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorBlock(ColorHandlerEvent.Block event) {
		event.getBlockColors().registerBlockColorHandler(BarrelModel.INSTANCE.colorizer, barrelBlock);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(BarrelModel.INSTANCE.colorizer, barrelItem);
		event.getItemColors().registerItemColorHandler(new ItemMinecartDayBarrel.Color(), barrelCartItem);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureMap(TextureStitchEvent.Pre event) {
		BarrelModel.INSTANCE.onTextureLoad(event.getMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:barrel", "normal"), BarrelModel.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:barrel", "inventory"), BarrelModel.INSTANCE);

		BarrelModel.INSTANCE.template = RenderUtils.getModel(new ResourceLocation("charset:block/barrel"));
	}
}
