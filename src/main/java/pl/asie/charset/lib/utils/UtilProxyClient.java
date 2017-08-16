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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.FontRendererFancy;
import pl.asie.charset.lib.render.ParticleBlockDustCharset;
import pl.asie.charset.lib.render.model.IStateParticleBakedModel;
import pl.asie.charset.lib.resources.CharsetFakeResourcePack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class UtilProxyClient extends UtilProxyCommon {
	public static FontRenderer FONT_RENDERER_FANCY;

	@Override
	public void init() {
		super.init();

		try {
			FONT_RENDERER_FANCY = new FontRendererFancy(Minecraft.getMinecraft().fontRenderer);
		} catch (Exception e) {
			e.printStackTrace();
			FONT_RENDERER_FANCY = Minecraft.getMinecraft().fontRenderer;
		}
	}

	@Override
	public EntityPlayer getPlayer(INetHandler handler) {
		return (handler instanceof INetHandlerPlayClient || handler instanceof INetHandlerLoginClient)
				? Minecraft.getMinecraft().player : super.getPlayer(handler);
	}

	@Override
	public EntityPlayer findPlayer(MinecraftServer server, String name) {
		if (server == null) {
			if (Minecraft.getMinecraft().world != null) {
				return Minecraft.getMinecraft().world.getPlayerEntityByName(name);
			}
			return null;
		} else {
			return super.findPlayer(server, name);
		}
	}

	@Override
	public void registerItemModel(Item item, int meta, String name) {
		if (name.contains("#")) {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name.split("#")[0], name.split("#")[1]));
		} else {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"));
		}
	}

	@Override
	public World getLocalWorld(int dim) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			World w = Minecraft.getMinecraft().world;
			if (w != null && w.provider.getDimension() == dim) {
				return w;
			} else {
				return null;
			}
		} else {
			return DimensionManager.getWorld(dim);
		}
	}

	@Override
	public void onServerStop() {
		super.onServerStop();


	}

	@Override
	public boolean isCallingFromMinecraftThread() {
		return Minecraft.getMinecraft().isCallingFromMinecraftThread();
	}

	@Override
	public ListenableFuture<Object> addScheduledTask(Runnable runnable) {
		return Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void setCreativeTabIfNotPresent(IForgeRegistryEntry entry, CreativeTabs tab) {
		if (entry instanceof Block) {
			Block block = (Block) entry;
			if (block.getCreativeTabToDisplayOn() == null) {
				block.setCreativeTab(tab);
			}
		} else if (entry instanceof Item) {
			Item item = (Item) entry;
			if (item.getCreativeTab() == null) {
				item.setCreativeTab(tab);
			}
		}
	}

	@Override
	public void spawnBlockDustClient(World world, BlockPos pos, Random rand, float posX, float posY, float posZ, int numberOfParticles, float particleSpeed) {
		TextureAtlasSprite sprite;
		int tintIndex = -1;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockBase) {
			tintIndex = ((BlockBase) state.getBlock()).getParticleTintIndex();
		}

		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		if (model instanceof IStateParticleBakedModel) {
			state = state.getBlock().getExtendedState(state.getActualState(world, pos), world, pos);
			sprite = ((IStateParticleBakedModel) model).getParticleTexture(state);
		} else {
			sprite = model.getParticleTexture();
		}

		ParticleManager manager = Minecraft.getMinecraft().effectRenderer;

		for (int i = 0; i < numberOfParticles; i++) {
			double xSpeed = rand.nextGaussian() * particleSpeed;
			double ySpeed = rand.nextGaussian() * particleSpeed;
			double zSpeed = rand.nextGaussian() * particleSpeed;

			try {
				Particle particle = new ParticleBlockDustCharset(world, posX, posY, posZ, xSpeed, ySpeed, zSpeed, state, pos, sprite, tintIndex);
				manager.addEffect(particle);
			} catch (Throwable var16) {
				ModCharset.logger.warn("Could not spawn block particle!");
				return;
			}
		}
	}
}
