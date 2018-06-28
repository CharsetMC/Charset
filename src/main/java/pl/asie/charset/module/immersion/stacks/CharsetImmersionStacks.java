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

package pl.asie.charset.module.immersion.stacks;

import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.MethodHandleHelper;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.SoundUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@CharsetModule(
		name = "immersion.stacks",
		description = "Place things! In the world! And they stack!",
		profile = ModuleProfile.TESTING
)
public class CharsetImmersionStacks {
	public BlockStacks blockStacks;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		blockStacks = new BlockStacks();
	}

	@SubscribeEvent
	public void registerBlock(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockStacks, "stacks_decorative");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileEntityStacks.class, "stacks_decorative");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:stacks_decorative", "normal"), new RenderTileEntityStacks());
	}

	private static final Map<Class, Boolean> overridesOnBlockActivated = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getUseItem() == Event.Result.DENY) {
			return;
		}

		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty()) {
			if (!TileEntityStacks.canAcceptStackType(stack)) {
				return;
			}

			IBlockState state = event.getWorld().getBlockState(event.getPos());
			BlockPos pos = event.getPos();

			Boolean overridesOBA = overridesOnBlockActivated.computeIfAbsent(state.getBlock().getClass(),
					(c) -> {
						try {
							Method m = MethodHandleHelper.reflectMethodRecurse(c, "onBlockActivated", "func_180639_a",
									World.class, BlockPos.class, IBlockState.class, EntityPlayer.class,
									EnumHand.class, EnumFacing.class,
									float.class, float.class, float.class);
							return m.getDeclaringClass() != Block.class;
						} catch (Exception e) {
							ModCharset.logger.warn("Exception during recursive method check on " + c + "!");
							e.printStackTrace();
							return true; // The safer of the two assumptions.
						}
					}
			);

			boolean sneaking = event.getEntityPlayer().isSneaking();
			boolean fullStack = sneaking;
			if (overridesOBA) {
				if (fullStack) {
					fullStack = false;
				} else {
					return;
				}
			}

			if (!(state.getBlock() instanceof BlockStacks) && !state.getBlock().isReplaceable(event.getWorld(), event.getPos()) && event.getFace() != null) {
				if (!sneaking && event.getFace() != EnumFacing.UP) {
					return;
				}

				pos = pos.offset(event.getFace());
			}

			while (true) {
				state = event.getWorld().getBlockState(pos);
				if (state.getBlock() instanceof BlockStacks) {
					TileEntity tile = event.getWorld().getTileEntity(pos);
					if (tile instanceof TileEntityStacks) {
						if (((TileEntityStacks) tile).isFull()) {
							pos = pos.up();
						} else {
							break;
						}
					}
				} else {
					break;
				}
			}

			if (state.getBlock().isReplaceable(event.getWorld(), pos)) {
				state = blockStacks.getDefaultState();
				event.getWorld().setBlockState(pos, state);
			}

			if (state.getBlock() instanceof BlockStacks) {
				TileEntity tile = event.getWorld().getTileEntity(pos);
				if (tile instanceof TileEntityStacks) {
					int count = stack.getCount();
					for (int i = 0; i < (fullStack ? count : 1); i++) {
						if (stack.isEmpty()) {
							break;
						}

						ItemStack stackOffered = stack.copy();
						stackOffered.setCount(1);

						if (((TileEntityStacks) tile).offerStack(false, stackOffered, event.getHitVec(), fullStack)) {
							SoundUtils.playSoundRemote(
									event.getEntityPlayer(), event.getHitVec(),
									64.0D,
									SoundEvents.BLOCK_METAL_PLACE,
									SoundCategory.BLOCKS,
									0.5f,
									1.0f
							);

							if (!event.getEntityPlayer().isCreative()) {
								stack.shrink(stackOffered.getCount());
							}
							event.setCanceled(true);
							event.setCancellationResult(EnumActionResult.SUCCESS);
						}
					}

					if (((TileEntityStacks) tile).isEmpty()) {
						event.getWorld().setBlockToAir(pos);
					}
				}
			}
		}
	}
}
