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

package pl.asie.charset.lib.utils;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.render.ParticleBlockDustCharset;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Random;

public class UtilProxyCommon implements IThreadListener {
	@CharsetModule.SidedProxy(clientSide = "pl.asie.charset.lib.utils.UtilProxyClient", serverSide = "pl.asie.charset.lib.utils.UtilProxyCommon")
	public static UtilProxyCommon proxy;

	public World getWorld(INetHandler handler, int dim) {
		return DimensionManager.getWorld(dim);
	}

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

	public String getLanguageCode() {
		return "en_us";
	}

	public boolean addRunningParticles(IBlockState state, World world, BlockPos pos, Entity entity) {
		return true;
	}

	public void registerItemModel(Item item, int meta, String name) {

	}

	public void init() {
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

	public void setTabAndNameIfNotPresent(IForgeRegistryEntry entry, String name, CreativeTabs tab) {
		if (entry instanceof Block) {
			if (entry instanceof BlockBase) {
				((BlockBase) entry).verifyPreGameLaunch();
			}

			Block block = (Block) entry;
			if (block.getCreativeTabToDisplayOn() == null) {
				block.setCreativeTab(tab);
			}
			if (block.getUnlocalizedName() == null) {
				block.setUnlocalizedName("charset.missing_name." + name);
			}
		} else if (entry instanceof Item) {
			Item item = (Item) entry;
			if (item.getCreativeTab() == null) {
				item.setCreativeTab(tab);
			}
			if (item.getUnlocalizedName() == null) {
				item.setUnlocalizedName("charset.missing_name." + name);
			}
		}
	}

	public void spawnBlockDustClient(World world, BlockPos pos, Random rand, float posX, float posY, float posZ, int numberOfParticles, float particleSpeed, EnumFacing facing) {

	}

	public World getServerWorldOrDefault(World def) {
		return def;
	}
}
