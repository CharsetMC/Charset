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

package pl.asie.charset.module.crafting.cauldron;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.render.sprite.TextureWhitener;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.crafting.cauldron.api.CauldronContents;
import pl.asie.charset.module.crafting.cauldron.api.ICauldronRecipe;
import pl.asie.charset.module.crafting.cauldron.fluid.FluidDyedWater;
import pl.asie.charset.module.crafting.cauldron.fluid.FluidPotion;
import pl.asie.charset.module.crafting.cauldron.fluid.PotionFluidContainerItem;
import pl.asie.charset.module.crafting.cauldron.recipe.*;
import pl.asie.charset.module.crafting.cauldron.api.ICauldron;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CharsetModule(
		name = "crafting.cauldron",
		description = "Improved Cauldron!",
		profile = ModuleProfile.STABLE,
		antidependencies = "mod:inspirations"
)
public class CharsetCraftingCauldron {
	public static int waterBottleSize = 0;
	public static int waterAlpha = 180;
	public static BlockCauldronCharset blockCauldron;
	public static FluidDyedWater dyedWater;
	public static FluidPotion liquidPotion, liquidSplashPotion, liquidLingeringPotion;
	public static boolean enableLiquidPotions;
	public static int maxArrowTipMultiplier;
	private static List<ICauldronRecipe> recipeList = new ArrayList<>();

	@CharsetModule.Configuration
	public static Configuration config;

	public static void add(ICauldronRecipe recipe) {
		recipeList.add(recipe);
	}

	public static Optional<CauldronContents> craft(ICauldron cauldronCharset, CauldronContents contents) {
		for (ICauldronRecipe recipe : recipeList) {
			if (!recipe.matches(contents.getSource())) {
				continue;
			}

			Optional<CauldronContents> contentsNew = recipe.apply(cauldronCharset, contents);
			if (contentsNew.isPresent()) {
				return contentsNew;
			}
		}

		return Optional.empty();
	}

	@Mod.EventHandler
	public void onLoadConfig(CharsetLoadConfigEvent event) {
		waterBottleSize = ConfigUtils.getInt(config, "balance", "waterBottleSize", 250, 0, 1000, "Set the amount of water contained in one water bottle for Cauldron usage. Setting to 0 will approximate between 333 and 334 mB to mimic vanilla. A recommended value is 250. (Please note that setting this value to 0 will DISABLE liquid potions. You have been warned.)", true);
		enableLiquidPotions = ConfigUtils.getBoolean(config, "general", "enableLiquidPotions", true, "Enable liquid potions and glass bottles as fluid containers. Requires a non-zero waterBottleSize. Disable if you encounter compatibility issues.", true);
		maxArrowTipMultiplier = ConfigUtils.getInt(config, "balance", "maxArrowTipDivisor", 10, 1, 100, "waterBottleSize / maxArrowTipDivisor = the amount of mB of lingering potion required to tip an arrow. Has to actually be divisible.", false);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		blockCauldron = new BlockCauldronCharset();
		FluidRegistry.registerFluid(dyedWater = new FluidDyedWater("dyed_water"));
		dyedWater.setUnlocalizedName("charset.dyed_water");

		if (waterBottleSize > 0 && enableLiquidPotions) {
			FluidRegistry.registerFluid(liquidPotion = new FluidPotion("charset_potion", "item.potion.name"));
			liquidPotion.setUnlocalizedName("charset.potion");
			FluidRegistry.registerFluid(liquidSplashPotion = new FluidPotion("charset_potion_splash", "item.splash_potion.name"));
			liquidSplashPotion.setUnlocalizedName("charset.potion_splash");
			FluidRegistry.registerFluid(liquidLingeringPotion = new FluidPotion("charset_potion_lingering", "item.lingering_potion.name"));
			liquidLingeringPotion.setUnlocalizedName("charset.potion_lingering");

			MinecraftForge.EVENT_BUS.register(new PotionFluidContainerItem.Provider());
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		recipeList.add(new RecipeDyeWater());
		recipeList.add(new RecipeDyeItem());
		recipeList.add(new RecipeDyeItemPure()); // has to go after RecipeDyeItem to emit error on impure dye after handling impure dye recipes
		recipeList.add(new RecipeWashDyedWater());
		recipeList.add(new RecipeBucketCraft());

		if (liquidLingeringPotion != null) {
			recipeList.add(new RecipeTipArrow());
		}

		RegistryUtils.register(TileCauldronCharset.class, "improved_cauldron");
		FMLInterModComms.sendMessage("charset", "addLock", "minecraft:cauldron");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileCauldronCharset.class, new TileRendererCauldronCharset());
	}

	private static final ResourceLocation WATER_STILL = new ResourceLocation("minecraft:blocks/water_still");
	private static final ResourceLocation WATER_FLOWING = new ResourceLocation("minecraft:blocks/water_flow");

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		TextureWhitener.INSTANCE.remap(event.getMap(), WATER_STILL, FluidDyedWater.TEXTURE_STILL, WATER_STILL);
		TextureWhitener.INSTANCE.remap(event.getMap(), WATER_FLOWING, FluidDyedWater.TEXTURE_FLOWING, WATER_STILL);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		IBakedModel l0 = event.getModelRegistry().getObject(new ModelResourceLocation("minecraft:cauldron#level=0"));

		if (l0 != null) {
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=1"), l0);
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=2"), l0);
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=3"), l0);
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(blockCauldron.setRegistryName("minecraft:cauldron"));
	}

	@SubscribeEvent
	public void patchSneakClick(PlayerInteractEvent.RightClickBlock event) {
		if (event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockCauldronCharset) {
			event.setUseBlock(Event.Result.ALLOW);
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			event.getWorld().addEventListener(CauldronLevelUpdateListener.INSTANCE);
		}
	}
}
