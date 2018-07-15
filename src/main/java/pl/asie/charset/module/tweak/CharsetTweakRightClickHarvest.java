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

package pl.asie.charset.module.tweak;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.ThreeState;

@CharsetModule(
		name = "tweak.rightClickHarvest",
		description = "Harvest crops by right-clicking!",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakRightClickHarvest {
	private boolean whitelistRequired;

	@CharsetModule.Configuration
	public static Configuration config;

	@Mod.EventHandler
	public void onLoadConfig(CharsetLoadConfigEvent event) {
		whitelistRequired = ConfigUtils.getBoolean(config, "general", "harvestingRequiresWhitelisting", false, "Does right-click-harvesting a crop require whitelisting?", true);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		if (!world.isRemote && event.getHand() == EnumHand.MAIN_HAND && event.getEntityPlayer().isSneaking() && event.getEntityPlayer().getHeldItem(event.getHand()).isEmpty()) {
			BlockPos pos = event.getPos();
			IBlockState state = world.getBlockState(pos);

			if (state.getBlock() instanceof IGrowable
					&& !((IGrowable) state.getBlock()).canGrow(world, pos, state, false)
					&& !world.isAirBlock(pos.down())) {
				ThreeState rightClickHarvest = CharsetIMC.INSTANCE.allows("rightClickHarvest", state.getBlock().getRegistryName()).otherwise(whitelistRequired ? ThreeState.NO : ThreeState.YES);
				if (rightClickHarvest == ThreeState.NO) {
					return;
				}

				// It can no longer grow, so let's try dropping it.
				NonNullList<ItemStack> drops = NonNullList.create();
				state.getBlock().getDrops(drops, world, pos, state, 0);
				if (drops.size() >= 2) {
					ItemStack plantable = ItemStack.EMPTY;

					for (ItemStack stack : drops) {
						if (!stack.isEmpty() && stack.getItem() instanceof IPlantable) {
							if (plantable.isEmpty()) {
								plantable = stack;
							} else if (!ItemUtils.canMerge(plantable, stack)) {
								return; // 2+ plantables
							}
						}
					}

					if (!plantable.isEmpty()) {
						Vec3d hVec = event.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());

						world.setBlockToAir(pos);
						if (plantable.onItemUse(event.getEntityPlayer(), world, pos.down(), event.getHand(), EnumFacing.UP, (float) hVec.x, (float) hVec.y, (float) hVec.z) == EnumActionResult.SUCCESS) {
							plantable.shrink(1);
						}

						for (ItemStack stack : drops) {
							if (!stack.isEmpty()) {
								float distance = 0.125f;
								if (stack.getItem() instanceof IPlantable) {
									distance /= 2;
								}

								ItemUtils.spawnItemEntity(
										world, new Vec3d(pos).add(0.5, 0.5, 0.5),
										stack,
										distance, distance, distance,
										1.0f
								);
							}
						}

						event.setCanceled(true);
					}
				}
			}
		}
	}
}
