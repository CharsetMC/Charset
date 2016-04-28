package pl.asie.charset.tweaks;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Ported from the Minecraft mod "copycore" by copygirl.
 * <p/>
 * Copyright (c) 2014 copygirl
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class TweakDoubleDoors extends Tweak {
	private final Set<BlockDoor> allowedDoors = new HashSet<BlockDoor>();

	public TweakDoubleDoors() {
		super("tweaks", "doubleDoorAutoOpen", "Make double doors open both at the same time when one is opened.", true);
	}

	@Override
	public boolean init() {
		allowedDoors.add((BlockDoor) Blocks.ACACIA_DOOR);
		allowedDoors.add((BlockDoor) Blocks.BIRCH_DOOR);
		allowedDoors.add((BlockDoor) Blocks.JUNGLE_DOOR);
		allowedDoors.add((BlockDoor) Blocks.OAK_DOOR);
		allowedDoors.add((BlockDoor) Blocks.SPRUCE_DOOR);
		allowedDoors.add((BlockDoor) Blocks.DARK_OAK_DOOR);
		return true;
	}

	@Override
	public void enable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	private IBlockState getActualState(IBlockAccess access, BlockPos pos) {
		IBlockState state = access.getBlockState(pos);
		return state.getBlock().getActualState(state, access, pos);
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
			door.onBlockActivated(event.getWorld(), pos, other, event.getEntityPlayer(), event.getHand(), event.getItemStack(), event.getFace(), 0, 0, 0);
		}
	}
}
