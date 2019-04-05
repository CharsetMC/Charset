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

package pl.asie.charset.module.misc.scaffold;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

@CharsetModule(
		name = "misc.scaffold",
		description = "Adds scaffolds you can build up and climb on.",
		profile = ModuleProfile.STABLE
)
public class CharsetMiscScaffold {
	@CharsetModule.Instance
	public static CharsetMiscScaffold instance;

	public static BlockScaffold scaffoldBlock;
	public static ItemScaffold scaffoldItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!ForgeModContainer.fullBoundingBoxLadders) {
			ModCharset.logger.warn("To make Charset scaffolds work better, we recommend enabling fullBoundingBoxLadders in forge.cfg.");
		}

		scaffoldBlock = new BlockScaffold();
		scaffoldItem = new ItemScaffold(scaffoldBlock);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(scaffoldItem, 0, "charset:scaffold");
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), scaffoldBlock, "scaffold");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), scaffoldItem, "scaffold");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (scaffoldBlock != null) {
			RegistryUtils.register(TileScaffold.class, "scaffold");
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void addCustomModels(TextureStitchEvent.Pre event) {
		ModelScaffold.scaffoldModel = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/scaffold"), event.getMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:scaffold", "normal"), ModelScaffold.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:scaffold", "inventory"), ModelScaffold.INSTANCE);
	}

	@SubscribeEvent
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
		EnumHand hand = event.getHand();
		EnumFacing facing = event.getFace();
		EntityPlayer playerIn = event.getEntityPlayer();

		if (hand != EnumHand.MAIN_HAND) return;
		if (facing == null || facing.getAxis() == EnumFacing.Axis.Y) return;
		if (playerIn.isSneaking()) return;

		World worldIn = event.getWorld();
		BlockPos pos = event.getPos();

		ItemStack stack = playerIn.getHeldItem(hand);
		ItemStack oldStack = stack.copy();

		if (!stack.isEmpty() && stack.getItem() instanceof ItemScaffold) {
			if (worldIn.getBlockState(pos).getBlock() instanceof BlockScaffold) {
				event.setCanceled(true);

				for (int overhang = 1; overhang < BlockScaffold.MAX_OVERHANG; overhang++) {
					BlockPos targetPos = new BlockPos(pos.getX(), pos.getY() + overhang, pos.getZ());
					if (worldIn.isValid(targetPos) && stack.getItem().onItemUse(playerIn, worldIn, targetPos, hand, EnumFacing.DOWN, 0.5f, 0.0f, 0.5f) == EnumActionResult.SUCCESS) {
						if (playerIn.isCreative()) {
							playerIn.setHeldItem(hand, oldStack);
						}
						return;
					}

					if (worldIn.isAirBlock(targetPos)) {
						return;
					}
				}
			}
		}
	}
}
