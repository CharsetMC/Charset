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

package pl.asie.charset.module.storage.locks;

import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

@CharsetModule(
		name = "storage.locks",
		description = "Player interaction-preventing locks and keys"
)
public class CharsetStorageLocks {
	@CharsetModule.Instance
	public static CharsetStorageLocks instance;

	@CharsetModule.Configuration
	public static Configuration config;

	public static final Random rand = new Random();
	public static final int DEFAULT_LOCKING_COLOR = 0xFBDB6A;

	public static ItemMasterKey masterKeyItem;
	public static ItemKey keyItem;
	public static ItemLock lockItem;

	public static boolean enableKeyKeepInventory;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		masterKeyItem = new ItemMasterKey();
		keyItem = new ItemKey();
		lockItem = new ItemLock();

		enableKeyKeepInventory = config.getBoolean("keepKeysOnDeath", "locks", true, "Should keys be kept in inventory on death?");
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(masterKeyItem, 0, "charset:masterKey");
		RegistryUtils.registerModel(keyItem, 0, "charset:key");
		RegistryUtils.registerModel(lockItem, 0, "charset:lock");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(masterKeyItem.setRegistryName("masterKey"));
		event.getRegistry().register(keyItem.setRegistryName("key"));
		event.getRegistry().register(lockItem.setRegistryName("lock"));
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		IRecipe recipeNewKey = new ShapedOreRecipe(new ResourceLocation("charset:newKey"), new ItemStack(keyItem), "ng", "ng", " g", 'n', "nuggetGold", 'g', "ingotGold") {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack result = output.copy();
				result.setTagCompound(new NBTTagCompound());
				result.getTagCompound().setString("key", new UUID(rand.nextLong(), rand.nextLong()).toString());
				return result;
			}
		};

		IRecipe recipeDuplicateKey = new ShapedOreRecipe(new ResourceLocation("charset:duplicateKey"), new ItemStack(keyItem), "ng", "ng", "kg", 'n', "nuggetGold", 'g', "ingotGold", 'k', new ItemStack(keyItem, 1, 0)) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(0, 2);
				if (key.isEmpty()) {
					key = inv.getStackInRowAndColumn(1, 2);
				}

				if (!key.isEmpty() && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getKey(key));
					return result;
				}
				return null;
			}

			@Override
			public boolean isHidden() {
				return true;
			}
		};

		IRecipe recipeLock = new ShapedOreRecipe(new ResourceLocation("charset:lock"), new ItemStack(lockItem), " g ", "gkg", "gig", 'i', "ingotIron", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(1, 1);
				if (!key.isEmpty() && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getKey(key));
					return result;
				}
				return null;
			}
		};

		event.getRegistry().register(recipeNewKey.setRegistryName(new ResourceLocation(recipeNewKey.getGroup())));
		event.getRegistry().register(recipeDuplicateKey.setRegistryName(new ResourceLocation(recipeDuplicateKey.getGroup())));
		event.getRegistry().register(recipeLock.setRegistryName(new ResourceLocation(recipeLock.getGroup())));
		event.getRegistry().register(new RecipeDyeLock("charset:dyeLock").setRegistryName("charset:dyeLock"));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(EntityLock.class, "lock", 64, 3, false);

		MinecraftForge.EVENT_BUS.register(new LockEventHandler());

		if (enableKeyKeepInventory) {
			CharsetLib.deathHandler.addPredicate(new Predicate<ItemStack>() {
				@Override
				public boolean apply(@Nullable ItemStack input) {
					return input != null && input.getItem() instanceof ItemKey;
				}
			});
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityLock.class, manager -> new RenderLock(manager));
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemLock.Color(), CharsetStorageLocks.keyItem);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemLock.Color(), CharsetStorageLocks.lockItem);
	}
}
