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

		ThreeState instantGrowth = CharsetIMC.INSTANCE.allows("instantBonemeal", block.getRegistryName());
		ThreeState growth = CharsetIMC.INSTANCE.allows("bonemeal", block.getRegistryName());
		if (instantGrowth == ThreeState.MAYBE) {
			instantGrowth = ThreeState.from(instantGrowthDefault);
		}
		if (growth == ThreeState.MAYBE) {
			growth = growthWhitelist ? ThreeState.NO : ThreeState.YES;
		}

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
			event.getWorld().setBlockState(event.getPos(), stateNew, 2);
			event.setResult(Event.Result.ALLOW);
		}
	}
}
