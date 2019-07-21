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

package pl.asie.charset.module.tweak.coloredSlimeBlocks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pl.asie.charset.lib.capability.lib.WashableItem;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.render.sprite.TextureWhitener;

import java.util.Map;
import java.util.Optional;

@CharsetModule(
		name = "tweak.coloredSlimeBlocks",
		description = "Colored slime blocks!",
		profile = ModuleProfile.TESTING
)
public class CharsetTweakColoredSlimeBlocks {
	static BlockSlimeColored blockSlime;
	static ItemBlockSlimeColored itemSlime;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		blockSlime = new BlockSlimeColored();
		blockSlime.setRegistryName("charset:slime_block_colored");

		itemSlime = new ItemBlockSlimeColored(blockSlime);
		itemSlime.setRegistryName("charset:slime_block_colored");
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(blockSlime);
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(itemSlime);
	}

	private static final ResourceLocation WASHABLE_LOC = new ResourceLocation("charset", "colored_slime_block_washable");

	@SubscribeEvent
	public void onAttachItemCaps(AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() == itemSlime) {
			event.addCapability(WASHABLE_LOC, new WashableItem() {
				@Override
				public Optional<ItemStack> wash(ItemStack input) {
					input.shrink(1);
					return Optional.of(new ItemStack(Blocks.SLIME_BLOCK, 1, 0));
				}
			});
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRegisterBlockColorHandler(ColorHandlerEvent.Block event) {
		event.getBlockColors().registerBlockColorHandler(SlimeBlockColorHandler.INSTANCE, blockSlime);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRegisterItemColorHandler(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(SlimeBlockColorHandler.INSTANCE, itemSlime);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegistry(ModelRegistryEvent event) {
		final ModelResourceLocation loc = new ModelResourceLocation("charset:slime_block_colored");
		ModelLoader.setCustomStateMapper(blockSlime, blockIn -> {
			ImmutableMap.Builder<IBlockState, ModelResourceLocation> builder = new ImmutableMap.Builder<>();
			for (IBlockState state : blockIn.getBlockState().getValidStates()) {
				builder.put(state, loc);
			}
			return builder.build();
		});
		for (int i = 0; i < 16; i++) {
			ModelLoader.setCustomModelResourceLocation(itemSlime, i, loc);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		event.getMap().setTextureEntry(
				TextureWhitener.INSTANCE.remap(
						new ResourceLocation("minecraft:blocks/slime"),
						new ResourceLocation("charset:blocks/slime_block"),
						new ResourceLocation("minecraft:blocks/slime")
				)
		);
	}
}
