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

package pl.asie.charset.storage.locks;

import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.annotation.CharsetModule;
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
		GameRegistry.register(masterKeyItem.setRegistryName("masterKey"));

		keyItem = new ItemKey();
		GameRegistry.register(keyItem.setRegistryName("key"));

		lockItem = new ItemLock();
		GameRegistry.register(lockItem.setRegistryName("lock"));

		RegistryUtils.registerModel(masterKeyItem, 0, "charset:masterKey");
		RegistryUtils.registerModel(keyItem, 0, "charset:key");
		RegistryUtils.registerModel(lockItem, 0, "charset:lock");

		MinecraftForge.EVENT_BUS.register(this);

		enableKeyKeepInventory = config.getBoolean("keepKeysOnDeath", "locks", true, "Should keys be kept in inventory on death?");
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

		IRecipe recipeNewKey = new ShapedOreRecipe(new ItemStack(keyItem), "ng", "ng", " g", 'n', "nuggetGold", 'g', "ingotGold") {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack result = output.copy();
				result.setTagCompound(new NBTTagCompound());
				result.getTagCompound().setString("key", new UUID(rand.nextLong(), rand.nextLong()).toString());
				return result;
			}
		};

		IRecipe recipeDuplicateKey = new ShapedOreRecipe(new ItemStack(keyItem), "ng", "ng", "kg", 'n', "nuggetGold", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(0, 2);
				if (key.isEmpty()) {
					key = inv.getStackInRowAndColumn(1, 2);
				}

				if (!key.isEmpty() && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getRawKey(key));
					return result;
				}
				return null;
			}
		};

		IRecipe recipeLock = new ShapedOreRecipe(new ItemStack(lockItem), " g ", "gkg", "gig", 'i', "ingotIron", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(1, 1);
				if (!key.isEmpty() && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getRawKey(key));
					return result;
				}
				return null;
			}
		};

		GameRegistry.addRecipe(recipeNewKey);
		GameRegistry.addRecipe(recipeDuplicateKey);
		GameRegistry.addRecipe(recipeLock);
		RecipeSorter.register("charset:keyNew", recipeNewKey.getClass(), RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
		RecipeSorter.register("charset:keyDuplicate", recipeDuplicateKey.getClass(), RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
		RecipeSorter.register("charset:lockNew", recipeLock.getClass(), RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

		GameRegistry.addRecipe(new RecipeDyeLock());
		RecipeSorter.register("charset:lockDye", RecipeDyeLock.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
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
