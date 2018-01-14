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

package pl.asie.charset.module.storage.locks;

import net.minecraft.item.Item;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.patchwork.LocksCapabilityHook;
import pl.asie.charset.patchwork.PatchworkHelper;

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
	public void loadConfig(CharsetLoadConfigEvent event) {
		enableKeyKeepInventory = ConfigUtils.getBoolean(config, "general", "keepKeysOnDeath", true, "Should keys be kept in inventory on death?", true);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		masterKeyItem = new ItemMasterKey();
		keyItem = new ItemKey();
		keyringItem = new ItemKeyring();
		lockItem = new ItemLock();

		ModCharset.dataFixes.registerFix(FixTypes.ITEM_INSTANCE, new FixCharsetLockKeyTagChange());
		
		if (PatchworkHelper.getBoolean("LOCKS_BLOCK_CAPABILITIES")) {
			LocksCapabilityHook.handler = new LocksCapabilityHandler();

			LocksCapabilityHandler.addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, true);
			LocksCapabilityHandler.addCapability(Capabilities.ITEM_INSERTION_HANDLER, true);
			LocksCapabilityHandler.addCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, true);

			CharsetStorageLocks.config.getCategory("blockedCapabilities").setComment("NOTE: This functionality requires a special configuration option in CharsetPatches to be turned on! Otherwise, it will not be effective!");

			if (config.hasChanged()) {
				config.save();
			}
		}
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
		RegistryUtils.register(event.getRegistry(), masterKeyItem, "masterKey");
		RegistryUtils.register(event.getRegistry(), keyItem, "key");
		RegistryUtils.register(event.getRegistry(), keyringItem, "keyring");
		RegistryUtils.register(event.getRegistry(), lockItem, "lock");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(EntityLock.class, "lock", 64, 3, false);

		MinecraftForge.EVENT_BUS.register(new LockEventHandler());

		if (enableKeyKeepInventory) {
			CharsetLib.deathHandler.addPredicate(input -> input != null && input.getItem() instanceof ItemKey);
		}

		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.KEYRING, Side.SERVER, (r) -> new ContainerKeyring(r.player.inventory));
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityLock.class, RenderLock::new);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerColorItem(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(new ItemLock.Color(), CharsetStorageLocks.keyItem);
		event.getItemColors().registerItemColorHandler(new ItemLock.Color(), CharsetStorageLocks.lockItem);
		event.getItemColors().registerItemColorHandler(new ItemKeyring.Color(), CharsetStorageLocks.keyringItem);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.KEYRING, Side.CLIENT, (r) -> new GuiKeyring(r.getContainer()));
	}
}
