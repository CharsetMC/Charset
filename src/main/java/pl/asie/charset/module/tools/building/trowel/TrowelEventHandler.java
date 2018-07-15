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

package pl.asie.charset.module.tools.building.trowel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.utils.EntityUtils;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrowelEventHandler {
	@CapabilityInject(TrowelCache.class)
	public static Capability<TrowelCache> CACHE;
	private static final ResourceLocation CACHE_LOC = new ResourceLocation("charset:trowel_cache");
	private static CapabilityProviderFactory<TrowelCache> CACHE_FACTORY;

	private static List<TrowelHandler> HANDLERS = new ArrayList<>();

	public static void init() {
		CapabilityManager.INSTANCE.register(TrowelCache.class, DummyCapabilityStorage.get(), TrowelCache::new);
		MinecraftForge.EVENT_BUS.register(new TrowelEventHandler());
	}

	@SubscribeEvent
	public void onAttach(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			if (CACHE_FACTORY == null) {
				CACHE_FACTORY = new CapabilityProviderFactory<>(CACHE);
			}

			event.addCapability(CACHE_LOC, CACHE_FACTORY.create(new TrowelCache()));
		}
	}

	public static void registerHandler(TrowelHandler handler) {
		HANDLERS.add(handler);
	}

	public static Optional<TrowelHandler> getHandler(EntityLivingBase player, EnumHand hand) {
		if (player.getHeldItem(hand).isEmpty() || (player instanceof EntityPlayer && (((EntityPlayer) player).isCreative() || ((EntityPlayer) player).isSpectator()))) {
			return Optional.empty();
		}

		Optional<TrowelHandler> handler = HANDLERS.stream().filter((h) -> h.matches(player, hand)).findFirst();
		if (handler.isPresent()) {
			return handler;
		} else {
			if (TrowelHandlerDefault.INSTANCE.matches(player, hand)) {
				return Optional.of(TrowelHandlerDefault.INSTANCE);
			} else {
				return Optional.empty();
			}
		}
	}

	public static boolean isUsingTrowel(EntityPlayer player) {
		if (player == null || EntityUtils.isPlayerFake(player)) {
			return false;
		}

		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		return !stack.isEmpty() && stack.getItem() instanceof ItemTrowel;
	}

	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (isUsingTrowel(event.getEntityPlayer()) && event.getPos() != null) {
			IBlockState heldState = ItemUtils.getBlockState(event.getEntityPlayer().getHeldItem(EnumHand.OFF_HAND));
			if (heldState != null && heldState != Blocks.AIR.getDefaultState()) {
				float digSpeedBase = event.getNewSpeed();
				float digSpeed = event.getEntityPlayer().getDigSpeed(heldState);
				if (digSpeedBase >= digSpeed) {
					event.setNewSpeed(Float.MAX_VALUE);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		EntityPlayer player = event.getEntityPlayer();
		if (isUsingTrowel(player)) {
			if (!getHandler(player, EnumHand.OFF_HAND).isPresent()) {
				event.setCanceled(true);
			}

			ItemStack stack = player.getHeldItem(EnumHand.OFF_HAND);
			TrowelCache cache = null;

			if (player.hasCapability(TrowelEventHandler.CACHE, null)) {
				cache = player.getCapability(TrowelEventHandler.CACHE, null);
			}

			if (cache != null && !cache.check(player.getEntityWorld(), stack, event.getPos())) {
				event.setCanceled(true);
			}
		}
	}
}
