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

package pl.asie.charset.lib.utils;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.render.ParticleBlockDustCharset;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;

import java.util.Random;

public class UtilProxyCommon implements IThreadListener {
	@SidedProxy(clientSide = "pl.asie.charset.lib.utils.UtilProxyClient", serverSide = "pl.asie.charset.lib.utils.UtilProxyCommon", modId = ModCharset.MODID)
	public static UtilProxyCommon proxy;

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

	public void setCreativeTabIfNotPresent(IForgeRegistryEntry entry, CreativeTabs tab) {

	}

	public void spawnBlockDustClient(World world, BlockPos pos, Random rand, float posX, float posY, float posZ, int numberOfParticles, float particleSpeed) {

	}
}
