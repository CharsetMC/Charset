/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.storage.chests;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.recipe.RecipeDummy;
import pl.asie.charset.lib.recipe.RecipePatchwork;
import pl.asie.charset.lib.render.sprite.TextureWhitener;
import pl.asie.charset.lib.resources.CharsetFakeResourcePack;
import pl.asie.charset.lib.inventory.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.BiPredicate;

@CharsetModule(
		name = "storage.chests",
		description = "Chests out of any wood!",
		profile = ModuleProfile.TESTING
)
public class CharsetStorageChests {
	@CharsetModule.Configuration
	public static Configuration config;
	private static boolean disableVanillaChestRecipe;

	public static BlockChestCharset blockChest;
	public static ItemBlock itemChest;

	@Mod.EventHandler
	public void loadConfig(CharsetLoadConfigEvent event) {
		disableVanillaChestRecipe = ConfigUtils.getBoolean(config, "general", "disableVanillaChestRecipe", true, "Disable the vanilla chest recipe. May cause issues with auto-crafting mods which do not expect single-wood requirements in an IRecipe.", true);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		blockChest = new BlockChestCharset();
		itemChest = new ItemBlockChestCharset(blockChest);

		CapabilityHelper.registerBlockProvider(Capabilities.CUSTOM_CARRY_PROVIDER, blockChest, (a, b, c, d) -> CustomCarryHandlerChest::new);
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		IRecipe patchwork = new RecipePatchwork((stack) -> {
			if (stack.getItem() == itemChest) {
				return new ItemStack(Blocks.CHEST);
			} else {
				return stack;
			}
		});

		RegistryUtils.register(event.getRegistry(), patchwork, "replace_chest");
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		OreDictionary.registerOre("chest", itemChest);
		OreDictionary.registerOre("chestWood", itemChest);

		if (disableVanillaChestRecipe) {
			Iterator<IRecipe> iterator = CraftingManager.REGISTRY.iterator();
			while (iterator.hasNext()) {
				IRecipe recipe = iterator.next();
				ItemStack output = recipe.getRecipeOutput();
				if (!output.isEmpty() && output.getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
					ForgeRegistries.RECIPES.register(new RecipeDummy(recipe.getGroup()).setRegistryName(recipe.getRegistryName()));
					ModCharset.logger.info("Disabled " + Item.REGISTRY.getNameForObject(output.getItem()).toString() + " (removed recipe)");
				}
			}
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		addFakeTexture(
				new ResourceLocation("minecraft:entity/chest/normal"),
				TileEntityChestRendererCharset.TEXTURE_NORMAL,
				(x, y) -> x >= 14/64f || y >= 14/64f
		);

		addFakeTexture(
				new ResourceLocation("minecraft:entity/chest/normal_double"),
				TileEntityChestRendererCharset.TEXTURE_NORMAL_DOUBLE,
				(x, y) -> x >= 14/128f || y >= 14/64f
		);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileEntityChestCharset.class, "chest");
		FMLInterModComms.sendMessage("charset", "addCarry", blockChest.getRegistryName());

		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.CHEST, Side.SERVER, (r) -> {
			TileEntity tile = r.getTileEntity();
			if (tile instanceof TileEntityChestCharset) {
				return new ContainerChestCharset((TileEntityChestCharset) tile, r.player.inventory);
			} else {
				return null;
			}
		});
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.CHEST, Side.CLIENT, (r) -> new GuiChestCharset(r.getContainer(ContainerChestCharset.class)));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChestCharset.class, TileEntityChestRendererCharset.INSTANCE);
		ForgeHooksClient.registerTESRItemStack(itemChest, 0, TileEntityChestCharset.class);
		itemChest.setTileEntityItemStackRenderer(new TileEntityChestRendererCharset.Stack());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegistry(ModelRegistryEvent event) {
		ModelLoader.setCustomModelResourceLocation(itemChest, 0, new ModelResourceLocation("minecraft:chest", "inventory"));
	}

	@SideOnly(Side.CLIENT)
	private void overrideChestModel(ModelBakeEvent event, String variant) {
		IBakedModel model = event.getModelRegistry().getObject(new ModelResourceLocation("minecraft:chest", variant));
		if (model == null) {
			model = ModelLoaderRegistry.getMissingModel().bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
		}

		event.getModelRegistry().putObject(new ModelResourceLocation("charset:chest", variant), new ModelChestCharset(model));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		overrideChestModel(event, "facing=west");
		overrideChestModel(event, "facing=east");
		overrideChestModel(event, "facing=north");
		overrideChestModel(event, "facing=south");
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockChest, "chest");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemChest, "chest");
	}

	@SideOnly(Side.CLIENT)
	private void addFakeTexture(ResourceLocation from, ResourceLocation res, BiPredicate<Float, Float> usePixel) {
		CharsetFakeResourcePack.INSTANCE.registerEntry(
				res,
				(stream) -> {
					BufferedImage image = RenderUtils.getTextureImage(from, ModelLoader.defaultTextureGetter());
					int[] pixels = new int[image.getWidth() * image.getHeight()];
					image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
					TextureWhitener.INSTANCE.remap(
							pixels, image.getWidth(), ModelLoader.defaultTextureGetter(),
							from, -1, usePixel, 1f, false
					);
					image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

					try {
						ImageIO.write(image, "png", stream);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		);
	}
}
