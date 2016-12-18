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

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMinecart;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.storage.barrel.EntityMinecartDayBarrel;
import pl.asie.charset.tweaks.tnt.EntityTNTImproved;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = ModCharsetCarts.MODID, name = ModCharsetCarts.NAME, version = ModCharsetCarts.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL, acceptableRemoteVersions = ModCharsetLib.ACCEPTABLE_REMOTE_VERSIONS)
public class ModCharsetCarts {
	public static final Map<Class<? extends Entity>, Class<? extends EntityMinecart>> REPLACEMENT_MAP = new HashMap<>();
	public static final String MODID = "CharsetCarts";
	public static final String NAME = "U";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.carts.ProxyClient", serverSide = "pl.asie.charset.carts.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetCarts instance;
	public static Logger logger;

	private Configuration config;

	private void register(Class<? extends EntityMinecart> minecart, String name) {
		EntityRegistry.registerModEntity(minecart, "charsetcarts:" + name, 2, this, 64, 1, true);
	}

	private void register(Class<? extends EntityMinecart> minecart, String name, Class<? extends Entity> from) {
		register(minecart, name);
		REPLACEMENT_MAP.put(from, minecart);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetCarts.MODID);
		config = new Configuration(ModCharsetLib.instance.getConfigFile("carts.cfg"));

		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		register(EntityMinecartImproved.class, "rminecart", EntityMinecart.class);

		MinecraftForge.EVENT_BUS.register(this);
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
				event.getWorld().spawnEntityInWorld(minecart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
