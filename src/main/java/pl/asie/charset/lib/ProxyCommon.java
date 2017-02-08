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

package pl.asie.charset.lib;

import akka.routing.Listen;
import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.audio.manager.AudioStreamManagerServer;

public class ProxyCommon implements IThreadListener {
	// TODO 1.11
//	public void drawWireHighlight(PartWire wire) {
//	}

	public EntityPlayer getPlayer(INetHandler handler) {
		return handler instanceof NetHandlerPlayServer ? ((NetHandlerPlayServer) handler).player : null;
	}

	public EntityPlayer findPlayer(MinecraftServer server, String name) {
		for (EntityPlayerMP target : server.getPlayerList().getPlayers()) {
			if (target.getName().equals(name)) {
				return target;
			}
		}
		return null;
	}

	public void registerItemModel(Item item, int meta, String name) {

	}

	public void registerItemModel(Block block, int meta, String name) {
		registerItemModel(Item.getItemFromBlock(block), meta, name);
	}

	public void registerBlock(Block block, String name) {
		registerBlock(block, new ItemBlock(block), name);
	}

	public void registerBlock(Block block, Item item, String name) {
		registerBlock(block, item, name, ModCharsetLib.CREATIVE_TAB);
	}

	public void registerBlock(Block block, Item item, String name, CreativeTabs tab) {
		GameRegistry.register(block.setRegistryName(name));
		GameRegistry.register(item.setRegistryName(name));
		block.setCreativeTab(tab);
	}

	public void registerRecipeShaped(ItemStack output, Object... recipe) {
		GameRegistry.addRecipe(new ShapedOreRecipe(output, recipe));
	}

	public void registerRecipeShapeless(ItemStack output, Object... recipe) {
		GameRegistry.addRecipe(new ShapelessOreRecipe(output, recipe));
	}

	public void init() {
		AudioStreamManager.INSTANCE = new AudioStreamManagerServer();
	}

	public void onServerStop() {

	}

	public World getLocalWorld(int dim) {
		return DimensionManager.getWorld(dim);
	}

	@Override
	public boolean isCallingFromMinecraftThread() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread();
	}

	@Override
	public ListenableFuture<Object> addScheduledTask(Runnable runnable) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
	}

	public boolean isClient() {
		return false;
	}
}
