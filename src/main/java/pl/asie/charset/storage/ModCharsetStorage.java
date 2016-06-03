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

package pl.asie.charset.storage;

import com.google.common.base.Predicate;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.MiscUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.storage.backpack.HandlerBackpack;
import pl.asie.charset.storage.backpack.BlockBackpack;
import pl.asie.charset.storage.backpack.ItemBackpack;
import pl.asie.charset.storage.backpack.PacketBackpackOpen;
import pl.asie.charset.storage.backpack.TileBackpack;
import pl.asie.charset.storage.barrel.BarrelCartRecipe;
import pl.asie.charset.storage.barrel.BarrelEventListener;
import pl.asie.charset.storage.barrel.BarrelRegistry;
import pl.asie.charset.storage.barrel.BarrelUpgradeRecipes;
import pl.asie.charset.storage.barrel.BlockBarrel;
import pl.asie.charset.storage.barrel.EntityMinecartDayBarrel;
import pl.asie.charset.storage.barrel.ItemDayBarrel;
import pl.asie.charset.storage.barrel.ItemMinecartDayBarrel;
import pl.asie.charset.storage.barrel.TileEntityDayBarrel;
import pl.asie.charset.storage.locking.*;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

@Mod(modid = ModCharsetStorage.MODID, name = ModCharsetStorage.NAME, version = ModCharsetStorage.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetStorage {
	public static final String MODID = "CharsetStorage";
	public static final String NAME = "#";
	public static final String VERSION = "@VERSION@";
	public static final int DEFAULT_LOCKING_COLOR = 0xFBDB6A;
	private static final Random rand = new Random();

	@Mod.Instance(MODID)
	public static ModCharsetStorage instance;

	@SidedProxy(clientSide = "pl.asie.charset.storage.ProxyClient", serverSide = "pl.asie.charset.storage.ProxyCommon")
	public static ProxyCommon proxy;

	public static PacketRegistry packet;
	public static Logger logger;

	public static BlockBackpack backpackBlock;
	public static ItemMasterKey masterKeyItem;
	public static ItemKey keyItem;
	public static ItemLock lockItem;

	public static boolean enableBackpackOpenKey;
	public static boolean enableKeyKeepInventory;

	public static BlockBarrel barrelBlock;
	public static ItemDayBarrel barrelItem;
	public static ItemMinecartDayBarrel barrelCartItem;

	public static boolean renderBarrelText, renderBarrelItem, renderBarrelItem3D;

	private Configuration config;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetStorage.MODID);
		config = new Configuration(ModCharsetLib.instance.getConfigFile("storage.cfg"));

		backpackBlock = new BlockBackpack();
		GameRegistry.register(backpackBlock.setRegistryName("backpack"));
		GameRegistry.register(new ItemBackpack(backpackBlock).setRegistryName("backpack"));

		barrelBlock = new BlockBarrel();
		barrelItem = new ItemDayBarrel(barrelBlock);
		barrelCartItem = new ItemMinecartDayBarrel();
		GameRegistry.register(barrelBlock.setRegistryName("barrel"));
		GameRegistry.register(barrelItem.setRegistryName("barrel"));
		GameRegistry.register(barrelCartItem.setRegistryName("barrelCart"));

		MinecraftForge.EVENT_BUS.register(new BarrelEventListener());

		masterKeyItem = new ItemMasterKey();
		GameRegistry.register(masterKeyItem.setRegistryName("masterKey"));

		keyItem = new ItemKey();
		GameRegistry.register(keyItem.setRegistryName("key"));

		lockItem = new ItemLock();
		GameRegistry.register(lockItem.setRegistryName("lock"));

		ModCharsetLib.proxy.registerItemModel(barrelItem, 0, "charsetstorage:barrel");
		ModCharsetLib.proxy.registerItemModel(barrelCartItem, 0, "charsetstorage:barrelCart");
		ModCharsetLib.proxy.registerItemModel(backpackBlock, 0, "charsetstorage:backpack");
		ModCharsetLib.proxy.registerItemModel(masterKeyItem, 0, "charsetstorage:masterKey");
		ModCharsetLib.proxy.registerItemModel(keyItem, 0, "charsetstorage:key");
		ModCharsetLib.proxy.registerItemModel(lockItem, 0, "charsetstorage:lock");

		MinecraftForge.EVENT_BUS.register(proxy);

		enableBackpackOpenKey = config.getBoolean("enableOpenKeyBinding", "backpack", true, "Should backpacks be openable with a key binding?");
		enableKeyKeepInventory = config.getBoolean("keepKeysOnDeath", "locks", true, "Should keys be kept in inventory on death?");

		renderBarrelItem3D = config.getBoolean("renderItem3D", "barrels", false, "Should items use fancy 3D rendering?");
		renderBarrelItem = config.getBoolean("renderItem", "barrels", true, "Should items be rendered on barrels?");
		renderBarrelText = config.getBoolean("renderText", "barrels", true, "Should text be rendered on barrels?");

		config.save();

		proxy.preInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerTileEntity(TileBackpack.class, "charset:backpack");
		GameRegistry.registerTileEntity(TileEntityDayBarrel.class, "charset:barrel");
		EntityRegistry.registerModEntity(EntityLock.class, "charsetstorage:lock", 1, this, 64, 3, true);
		EntityRegistry.registerModEntity(EntityMinecartDayBarrel.class, "charsetstorage:barrelCart", 2, this, 64, 1, true);

		proxy.init();

		packet = new PacketRegistry(ModCharsetStorage.MODID);
		packet.registerPacket(0x01, PacketBackpackOpen.class);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerStorage());

		MinecraftForge.EVENT_BUS.register(new HandlerBackpack());
		MinecraftForge.EVENT_BUS.register(new LockEventHandler());

		if (enableKeyKeepInventory) {
			ModCharsetLib.deathHandler.addPredicate(new Predicate<ItemStack>() {
				@Override
				public boolean apply(@Nullable ItemStack input) {
					return input != null && input.getItem() instanceof ItemKey;
				}
			});
		}

		GameRegistry.addRecipe(new BarrelCartRecipe());
		BarrelUpgradeRecipes.addUpgradeRecipes();

		RecipeSorter.register("charsetstorage:barrelCart", BarrelCartRecipe.class, RecipeSorter.Category.SHAPELESS, "");

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(backpackBlock), "lgl", "scs", "lwl",
				'l', Items.LEATHER, 'c', "chestWood", 's', "stickWood", 'g', "ingotGold", 'w', Blocks.WOOL));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(keyItem), "ng", "ng", " g", 'n', "nuggetGold", 'g', "ingotGold") {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack result = output.copy();
				result.setTagCompound(new NBTTagCompound());
				result.getTagCompound().setString("key", new UUID(rand.nextLong(), rand.nextLong()).toString());
				return result;
			}
		});

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(keyItem), "ng", "ng", "kg", 'n', "nuggetGold", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(0, 2);
				if (key == null) {
					key = inv.getStackInRowAndColumn(1, 2);
				}

				if (key != null && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getRawKey(key));
					return result;
				}
				return null;
			}
		});

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lockItem), " g ", "gkg", "gig", 'i', "ingotIron", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(1, 1);
				if (key != null && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getRawKey(key));
					return result;
				}
				return null;
			}
		});

		GameRegistry.addRecipe(new RecipeDyeLock());
		RecipeSorter.register("charsetstorage:lockDye", RecipeDyeLock.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
	}

	private void checkPlankForBarrel(ItemStack log) {
		InventoryCrafting plankCrafting = RecipeUtils.getCraftingInventory(3, 3);
		plankCrafting.setInventorySlotContents(0, log);
		IRecipe plankRecipe = RecipeUtils.getMatchingRecipe(plankCrafting, null);

		if (plankRecipe != null) {
			ItemStack plank = plankRecipe.getCraftingResult(plankCrafting);
			if (plank != null) {
				plank.stackSize = 1;

				// The great slab search
				ItemStack slab = plank;

				InventoryCrafting slabCrafting = RecipeUtils.getCraftingInventory(3, 3);
				slabCrafting.setInventorySlotContents(6, plank);
				slabCrafting.setInventorySlotContents(7, plank);
				slabCrafting.setInventorySlotContents(8, plank);
				IRecipe slabRecipe = RecipeUtils.getMatchingRecipe(slabCrafting, null);

				if (slabRecipe != null) {
					ItemStack potentialSlab = slabRecipe.getCraftingResult(slabCrafting);
					if (potentialSlab != null && potentialSlab.getItem() != Item.getItemFromBlock(Blocks.WOODEN_SLAB)) {
						slab = potentialSlab;
					}
				}

				BarrelRegistry.INSTANCE.registerCraftable(log, slab);
			}
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		BarrelRegistry.INSTANCE.register(TileEntityDayBarrel.Type.CREATIVE, new ItemStack(Blocks.BEDROCK), new ItemStack(Blocks.DIAMOND_BLOCK));
		barrelCartItem.setMaxStackSize(new ItemStack(Items.CHEST_MINECART).getMaxStackSize()); // Railcraft compat

		// If you stop this from happening in postInit, please adjust TextureStitchEvent in ProxyClient
		BarrelRegistry.INSTANCE.registerCraftable(new ItemStack(Blocks.LOG, 1, 0), new ItemStack(Blocks.WOODEN_SLAB, 1, 0));
		BarrelRegistry.INSTANCE.registerCraftable(new ItemStack(Blocks.LOG, 1, 1), new ItemStack(Blocks.WOODEN_SLAB, 1, 1));
		BarrelRegistry.INSTANCE.registerCraftable(new ItemStack(Blocks.LOG, 1, 2), new ItemStack(Blocks.WOODEN_SLAB, 1, 2));
		BarrelRegistry.INSTANCE.registerCraftable(new ItemStack(Blocks.LOG, 1, 3), new ItemStack(Blocks.WOODEN_SLAB, 1, 3));
		BarrelRegistry.INSTANCE.registerCraftable(new ItemStack(Blocks.LOG2, 1, 0), new ItemStack(Blocks.WOODEN_SLAB, 1, 4));
		BarrelRegistry.INSTANCE.registerCraftable(new ItemStack(Blocks.LOG2, 1, 1), new ItemStack(Blocks.WOODEN_SLAB, 1, 5));

		for (ItemStack log : OreDictionary.getOres("logWood", false)) {
			if (log.getItem() == Item.getItemFromBlock(Blocks.LOG) || log.getItem() == Item.getItemFromBlock(Blocks.LOG2)) {
				continue;
			}

			try {
				if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
					for (int i = 0; i < 128; i++) { // I know Forestry and Binnie's Mods might be going high. Hopefully not higher.
						checkPlankForBarrel(new ItemStack(log.getItem(), 1, i));
					}
				} else {
					checkPlankForBarrel(log);
				}
			} catch (Exception e) {

			}
		}

		proxy.postInit();
	}
}
