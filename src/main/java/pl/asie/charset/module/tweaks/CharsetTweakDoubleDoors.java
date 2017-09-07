/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.tweaks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ThreeState;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@CharsetModule(
		name = "tweak.doubledoors",
		description = "Makes double doors open at the same time upon right-clicking just one",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakDoubleDoors {
	private final Set<BlockDoor> allowedDoors = new HashSet<BlockDoor>();

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		for (Block block : GameRegistry.findRegistry(Block.class)) {
			try {
				if (block != null && block instanceof BlockDoor) {
					ThreeState state = CharsetIMC.INSTANCE.allows("doubleDoor", block.getRegistryName());
					boolean allowed = false;

					if (state == ThreeState.MAYBE && !(block.getRegistryName().getResourceDomain().equals("malisisdoors"))) {
						Class c = block.getClass();
						Method m = ReflectionHelper.findMethod(c, "onBlockActivated", "func_180639_a",
								World.class, BlockPos.class, IBlockState.class, EntityPlayer.class,
								EnumHand.class, EnumFacing.class,
								float.class, float.class, float.class);
						if (m != null && m.getDeclaringClass() == BlockDoor.class) {
							allowed = true;
						}
					} else if (state == ThreeState.YES) {
						allowed = true;
					}

					if (allowed) {
						allowedDoors.add((BlockDoor) block);
						ModCharset.logger.info("[tweak.doubledoors] Allowing " + block.getRegistryName().toString());
					}
				}
			} catch (ReflectionHelper.UnableToFindMethodException e) {
				// This is fine.
			}
		}
	}

	private IBlockState getActualState(IBlockAccess access, BlockPos pos) {
		IBlockState state = access.getBlockState(pos);
		return state.getActualState(access, pos);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntityPlayer().isSneaking() || event.getWorld().isRemote) {
			return;
		}

		IBlockState state = getActualState(event.getWorld(), event.getPos());
		Block block = state.getBlock();

		if (!(block instanceof BlockDoor) || !allowedDoors.contains(block)) {
			return;
		}

		BlockDoor door = (BlockDoor) block;

		EnumFacing direction = state.getValue(BlockDoor.FACING);
		boolean isOpen = state.getValue(BlockDoor.OPEN);
		BlockDoor.EnumHingePosition isMirrored = state.getValue(BlockDoor.HINGE);

		BlockPos pos = event.getPos().offset(isMirrored == BlockDoor.EnumHingePosition.RIGHT ? direction.rotateYCCW() : direction.rotateY());
		IBlockState other = getActualState(event.getWorld(), pos);

		if (other.getBlock() == door &&
				other.getValue(BlockDoor.FACING) == direction &&
				other.getValue(BlockDoor.OPEN) == isOpen &&
				other.getValue(BlockDoor.HINGE) != isMirrored) {
			door.onBlockActivated(event.getWorld(), pos, other, event.getEntityPlayer(), event.getHand(), event.getFace(), 0, 0, 0);
		}
	}
}
