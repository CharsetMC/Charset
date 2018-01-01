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

package pl.asie.charset.module.tweak.slidingBlocks;

import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;

@CharsetModule(
		name = "tweak.slidingBlocks",
		description = "\"blocks on ice that are pushed by pistons keep sliding\" - neptunepink",
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetTweakSlide {
	public static class QueuedChange {
		public final World world;
		public final BlockPos pos;
		public final EnumFacing direction;

		public QueuedChange(World world, BlockPos pos, EnumFacing direction) {
			this.world = world;
			this.pos = pos;
			this.direction = direction;
		}
	}

	private static final Queue<QueuedChange> queue = new ArrayDeque<>();

	public static class Listener implements IWorldEventListener {
		private final World world;

		public Listener(World world) {
			this.world = world;
		}

		@Override
		public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
			if (oldState.getBlock() == Blocks.PISTON_EXTENSION && newState.getBlock() == Blocks.PISTON_HEAD) {
				BlockPistonExtension.EnumPistonType type = newState.getValue(BlockPistonExtension.TYPE);
				if (type == BlockPistonExtension.EnumPistonType.DEFAULT) {
					EnumFacing facing = newState.getValue(BlockPistonExtension.FACING);
					if (facing.getAxis() != EnumFacing.Axis.Y) {
						queue.add(new QueuedChange(worldIn, pos.offset(facing), facing));
					}
				}
			}
		}

		@Override
		public void notifyLightSet(BlockPos pos) {

		}

		@Override
		public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

		}

		@Override
		public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

		}

		@Override
		public void playRecord(SoundEvent soundIn, BlockPos pos) {

		}

		@Override
		public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

		}

		@Override
		public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

		}

		@Override
		public void onEntityAdded(Entity entityIn) {

		}

		@Override
		public void onEntityRemoved(Entity entityIn) {

		}

		@Override
		public void broadcastSound(int soundID, BlockPos pos, int data) {

		}

		@Override
		public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

		}

		@Override
		public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(EntitySlidingBlock.class, "sliding_block", 64, 1, true);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntitySlidingBlock.class, EntityRendererSlidingBlock::new);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			event.getWorld().addEventListener(new Listener(event.getWorld()));
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
			while (!queue.isEmpty()) {
				QueuedChange change = queue.remove();
				if (EntitySlidingBlock.canSlideOn(change.world, change.pos, null)) {
					EntitySlidingBlock block = new EntitySlidingBlock(
							change.world,
							change.pos,
							change.direction
					);
					change.world.spawnEntity(block);
				}
			}
		}
	}
}
