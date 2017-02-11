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

package pl.asie.charset.tweaks.broken;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.biome.BiomeRiver;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.annotation.CharsetModule;

/* @CharsetModule(
		name = "tweak.finiteWater",
		description = "Adds slightly novel finite water. WIP",
		isDefault = false
) */
public class CharsetTweakFiniteWater {
	private boolean isWater(IBlockState state) {
		return state.getMaterial() == Material.WATER;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	// TODO: override worldgen to give ocean water special support
	@SubscribeEvent
	public void onFluidSource(BlockEvent.CreateFluidSourceEvent event) {
		if (isWater(event.getState())) {
			World world = event.getWorld();

			if (event.getPos().getY() <= world.getSeaLevel()) {
				Biome b = event.getWorld().getBiome(event.getPos());
				if (b instanceof BiomeOcean || b instanceof BiomeRiver) {
					boolean isAir = false;

					for (int i = event.getPos().getY() + 1; i <= world.getSeaLevel(); i++) {
						BlockPos pos = new BlockPos(event.getPos().getX(), i, event.getPos().getZ());
						IBlockState state = world.getBlockState(pos);
						if (isAir) {
							if (!state.getBlock().isAir(state, world, pos)) {
								// disconnection, cancel
								event.setResult(Event.Result.DENY);
								return;
							}
						} else {
							if (state.getBlock().isAir(state, world, pos)) {
								isAir = true;
							} else if (!isWater(state)) {
								// disconnection, cancel
								event.setResult(Event.Result.DENY);
								return;
							}
						}
					}

					// connection found, do not cancel
					return;
				}
			}

			// has not returned, cancel
			event.setResult(Event.Result.DENY);
			return;
		}
	}
}
