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
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

@CharsetModule(
		name = "storage.locks",
		description = "Player interaction-preventing locks and keys",
		profile = ModuleProfile.TESTING
)
public class CharsetStorageLocks {
	@CharsetModule.Instance
	public static CharsetStorageLocks instance;

	@CharsetModule.Configuration
	public static Configuration config;

	public static final int DEFAULT_LOCKING_COLOR = 0xFBDB6A;

	public static ItemMasterKey masterKeyItem;
	public static ItemKey keyItem;
	public static ItemKeyring keyringItem;
	public static ItemLock lockItem;

	public static boolean enableKeyKeepInventory;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		masterKeyItem = new ItemMasterKey();
		keyItem = new ItemKey();
		keyringItem = new ItemKeyring();
		lockItem = new ItemLock();

		enableKeyKeepInventory = config.getBoolean("keepKeysOnDeath", "locks", true, "Should keys be kept in inventory on death?");
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(masterKeyItem, 0, "charset:masterKey");
		RegistryUtils.registerModel(keyItem, 0, "charset:key");
		RegistryUtils.registerModel(lockItem, 0, "charset:lock");

		RegistryUtils.registerModel(keyringItem, 0, "charset:keyring");
		for (int i = 1; i <= 8; i++) {
			RegistryUtils.registerModel(keyringItem, i, "charset:keyring#inventory_" + i);
		}
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(masterKeyItem.setRegistryName("masterKey"));
		event.getRegistry().register(keyItem.setRegistryName("key"));
		event.getRegistry().register(keyringItem.setRegistryName("keyring"));
		event.getRegistry().register(lockItem.setRegistryName("lock"));
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

		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.KEYRING, Side.SERVER, (r) -> new ContainerKeyring(r.player.inventory));
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
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemKeyring.Color(), CharsetStorageLocks.keyringItem);

		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.KEYRING, Side.CLIENT, (r) -> new GuiKeyring(new ContainerKeyring(r.player.inventory)));
	}
}
