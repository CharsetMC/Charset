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
 *
 * Copyright (c) 2014 copygirl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.charset.lib.loader.CharsetModule;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@CharsetModule(
		name = "tweak.doubledoors",
		description = "Makes double doors open at the same time upon right-clicking just one"
)
public class CharsetTweakDoubleDoors {
	private final Set<BlockDoor> allowedDoors = new HashSet<BlockDoor>();

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		for (Block block : GameRegistry.findRegistry(Block.class)) {
			try {
				if (block != null && block instanceof BlockDoor) {
					Class c = block.getClass();
					Method m = ReflectionHelper.findMethod(c, "onBlockActivated", "func_180639_a",
							World.class, BlockPos.class, IBlockState.class, EntityPlayer.class,
							EnumHand.class, EnumFacing.class,
							float.class, float.class, float.class);
					if (m != null && m.getDeclaringClass() == BlockDoor.class) {
						allowedDoors.add((BlockDoor) block);
						System.out.println("Allowing " + block.getRegistryName().toString());
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
