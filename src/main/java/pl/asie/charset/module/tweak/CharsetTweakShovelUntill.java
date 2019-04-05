/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tweak;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

import java.util.Iterator;
import java.util.Objects;

@CharsetModule(
		name = "tweak.shovelUntill",
		description = "Left-clicking farmland with a shovel turns it back into dirt.",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakShovelUntill {
	public static final ResourceLocation capabilityLocation = new ResourceLocation("charset:shovel_untill_holder");
	@CapabilityInject(BlockDelayHolder.class)
	public static Capability<BlockDelayHolder> capability;
	public static CapabilityProviderFactory<BlockDelayHolder> provider;

	public static class BlockDelayHolder {
		private final Object2LongMap<BlockPos> map = new Object2LongOpenHashMap<>();
		private int dimensionId = Integer.MIN_VALUE;

		public void addBlock(World world, BlockPos pos, int delay) {
			if (world.provider.getDimension() != dimensionId) {
				map.clear();
				dimensionId = world.provider.getDimension();
			}
			map.put(pos, world.getTotalWorldTime() + delay);
		}

		public boolean canBreak(World world, BlockPos pos) {
			if (world.provider.getDimension() != dimensionId) {
				map.clear();
				return true;
			}
			long time = world.getTotalWorldTime();
			return map.getOrDefault(pos, (long) 0) < time;
		}

		public void collectGarbage(World world) {
			if (world.provider.getDimension() != dimensionId) {
				map.clear();
				return;
			}

			long time = world.getTotalWorldTime();
			map.object2LongEntrySet().removeIf(blockPosEntry -> blockPosEntry.getLongValue() < time);
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(BlockDelayHolder.class, DummyCapabilityStorage.get(), BlockDelayHolder::new);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && (event.player.getEntityWorld().getWorldTime() % 100) == 37
				&& event.player.hasCapability(capability, null)) {
			event.player.getCapability(capability, null).collectGarbage(event.player.getEntityWorld());
		}
	}

	@SubscribeEvent
	public void attachCaps(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			if (provider == null) {
				provider = new CapabilityProviderFactory<>(capability);
			}
			event.addCapability(capabilityLocation, provider.create(new BlockDelayHolder()));
		}
	}

	@SubscribeEvent
	public void getBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (event.getEntityPlayer().hasCapability(capability, null)) {
			if (!event.getEntityPlayer().getCapability(capability, null).canBreak(event.getEntityPlayer().getEntityWorld(), event.getPos())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getHand() != EnumHand.MAIN_HAND || event.getFace() != EnumFacing.UP) {
			return;
		}

		if (!event.getEntityPlayer().hasCapability(capability, null)) {
			return;
		}

		ItemStack tool = event.getEntityPlayer().getHeldItem(event.getHand());
		if (!tool.isEmpty() && tool.getItem().getToolClasses(tool).contains("shovel")) {
			IBlockState state = event.getWorld().getBlockState(event.getPos());
			// if (state.getBlock().isFertile(event.getWorld(), event.getPos())) {
			if (state.getBlock() == Blocks.FARMLAND) {
				BlockEvent.BreakEvent fakeBreakEvent = new BlockEvent.BreakEvent(
						event.getWorld(), event.getPos(), state, event.getEntityPlayer()
				);

				if (MinecraftForge.EVENT_BUS.post(fakeBreakEvent)) {
					return;
				}

				Objects.requireNonNull(event.getEntityPlayer().getCapability(capability, null))
						.addBlock(event.getWorld(), event.getPos(), 3 /* 150 ms */);

				event.getWorld().setBlockState(event.getPos(), Blocks.DIRT.getDefaultState());
				event.getEntityPlayer().swingArm(event.getHand());
				event.setCanceled(true);
			}
		}
	}
}
