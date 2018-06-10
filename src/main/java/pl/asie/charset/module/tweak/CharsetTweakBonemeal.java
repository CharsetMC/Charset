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

import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ThreeState;

import java.util.Collection;

@CharsetModule(
		name = "tweak.bonemeal",
		description = "Options to restore classic bonemeal behaviour and whitelist bonemealable blocks.",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakBonemeal {
	private boolean instantGrowthDefault, growthWhitelist, heuristicEnabled;

	@CharsetModule.Configuration
	public static Configuration config;

	@Mod.EventHandler
	public void onLoadConfig(CharsetLoadConfigEvent event) {
		instantGrowthDefault = ConfigUtils.getBoolean(config, "general", "allowInstantGrowth", false, "Is instant growth (old-style bonemeal) the default? Setting this to true means any non-blacklisted blocks grow instantly, setting this to false means only whitelisted ones do.", true);
		growthWhitelist = ConfigUtils.getBoolean(config, "general", "growthRequiresWhitelisting", false, "Does bonemeal only work on whitelisted blocks?", true);
		heuristicEnabled = ConfigUtils.getBoolean(config, "general", "instantGrowthUseHeuristic", true, "Enables heuristic handling of modded crops. Disable if modded crops start growing strangely or too quickly - or whitelist/blacklist the individual blocks.", true);
	}

	private Integer getMax(PropertyInteger property) {
		Collection<Integer> values = property.getAllowedValues();
		if (values.size() == 0) return null;
		Integer v = null;
		for (Integer v2 : values) {
			if (v == null || v2 > v) {
				v = v2;
			}
		}
		return v;
	}

	private IBlockState withMax(IBlockState state, PropertyInteger property) {
		Integer gm = getMax(property);
		if (gm != null) {
			return state.withProperty(property, gm);
		} else {
			return null;
		}
	}

	@SubscribeEvent
	public void onApplyBonemeal(BonemealEvent event) {
		if (event.getWorld().isRemote) {
			return;
		}

		IBlockState state = event.getBlock();
		Block block = state.getBlock();
		IBlockState stateNew = null;

		ThreeState instantGrowth = CharsetIMC.INSTANCE.allows("instantBonemeal", block.getRegistryName()).otherwise(ThreeState.from(instantGrowthDefault));
		ThreeState growth = CharsetIMC.INSTANCE.allows("bonemeal", block.getRegistryName()).otherwise(growthWhitelist ? ThreeState.NO : ThreeState.YES);

		if (growth == ThreeState.NO) {
			event.setCanceled(true);
			return;
		} else if (instantGrowth == ThreeState.NO) {
			return;
		}

		// from now on, instant growth handler

		if (block instanceof BlockCrops) { // crops
			stateNew = ((BlockCrops) block).withAge(((BlockCrops) block).getMaxAge());
		} else if (state.getPropertyKeys().contains(BlockCocoa.AGE)) { // cocoa
			stateNew = withMax(state, BlockCocoa.AGE);
		} else if (block instanceof BlockMushroom) { // mushroom
			((BlockMushroom) block).grow(event.getWorld(), event.getWorld().rand, event.getPos(), state);
			event.setResult(Event.Result.ALLOW);
			return;
		} else if (block instanceof BlockSapling) { // saplings
			((BlockSapling) block).generateTree(event.getWorld(), event.getPos(), state, event.getWorld().rand);
			event.setResult(Event.Result.ALLOW);
			return;
		} else if (state.getPropertyKeys().contains(BlockStem.AGE)) { // stem
			stateNew = withMax(state, BlockStem.AGE);
		} else if (block instanceof BlockDoublePlant || block instanceof BlockTallGrass) {
			// do nothing, they already grow instantly
			return;
		} else if (block instanceof BlockGrass) {
			return;
		} else if (block instanceof IGrowable && heuristicEnabled) { // heuristic
			int i = 128;
			boolean canGrow = true;
			while (i-- > 0 && canGrow) {
				IBlockState stateTmp = event.getWorld().getBlockState(event.getPos());
				Block blockTmp = stateTmp.getBlock();
				if (blockTmp instanceof IGrowable) {
					if (((IGrowable) blockTmp).canGrow(event.getWorld(), event.getPos(), event.getBlock(), false)) {
						((IGrowable) blockTmp).grow(event.getWorld(), event.getWorld().rand, event.getPos(), event.getBlock());
						canGrow = ((IGrowable) blockTmp).canGrow(event.getWorld(), event.getPos(), event.getBlock(), false);
					}
				}
			}
			if (canGrow) {
				ModCharset.logger.warn("Found block " + block.getRegistryName() + " which insists on continuing to grow! Odd. Perhaps the Charset mod author would like to know more?");
			}
			event.setResult(Event.Result.ALLOW);
			return;
		} else { // no handler
			return;
		}

		if (stateNew != null) {
			if (stateNew != state) {
				event.getWorld().setBlockState(event.getPos(), stateNew, 2);
				event.setResult(Event.Result.ALLOW);
			} else {
				return;
			}
		}
	}
}
