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

package pl.asie.charset.module.tools.engineering;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.NoticeStyle;
import pl.asie.charset.lib.notify.component.NotificationComponentString;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StopwatchTracker implements IWorldEventListener {
	public enum AddPositionResult {
		START,
		END
	}

	private final World world;
	private final Map<String, BlockPos> startPosMap = new HashMap<>();
	private final Map<String, BlockPos> endPosMap = new HashMap<>();
	private final Multimap<BlockPos, String> startPosListeners = HashMultimap.create();
	private final Multimap<BlockPos, String> endPosListeners = HashMultimap.create();
	private final Object2LongMap<String> startPosTimes = new Object2LongOpenHashMap<>();

	public StopwatchTracker() {
		this.world = null;
		startPosTimes.defaultReturnValue(-1);
	}

	public StopwatchTracker(World world) {
		this.world = world;
		startPosTimes.defaultReturnValue(-1);
		world.addEventListener(this);
	}

	public boolean clearPosition(String key) {
		BlockPos startPos = startPosMap.remove(key);
		BlockPos endPos = endPosMap.remove(key);

		if (startPos != null) {
			startPosListeners.remove(startPos, key);
		}

		if (endPos != null) {
			endPosListeners.remove(endPos, key);
		}

		return startPos != null || endPos != null;
	}

	public AddPositionResult addPosition(String key, BlockPos pos) {
		if (endPosMap.containsKey(key)) {
			clearPosition(key);
		}

		if (startPosMap.containsKey(key)) {
			endPosMap.put(key, pos);
			endPosListeners.put(pos, key);
			return AddPositionResult.END;
		} else {
			startPosMap.put(key, pos);
			startPosListeners.put(pos, key);
			return AddPositionResult.START;
		}
	}

	private boolean notify(EntityPlayer player, ItemStack stack, String key, BiConsumer<EntityPlayer, ItemStack> onFound) {
		if (!stack.isEmpty() && stack.getItem() instanceof ItemStopwatch) {
			String stackKey = ((ItemStopwatch) stack.getItem()).getKey(stack);
			if (key.equals(stackKey)) {
				onFound.accept(player, stack);
				return true;
			}
		}

		return false;
	}

	public void notify(String key, BiConsumer<EntityPlayer, ItemStack> onFound) {
		for (EntityPlayer player : world.playerEntities) {
			for (int i = 0; i < 10; i++) {
				ItemStack stack = i == 9 ? player.getHeldItemOffhand() : player.inventory.getStackInSlot(i);
				if (notify(player, stack, key, onFound)) {
					break;
				}
			}
		}
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		for (String s : endPosListeners.get(pos)) {
			long spt = startPosTimes.removeLong(s);
			if (spt >= 0) {
				long time = worldIn.getTotalWorldTime() - spt;
				if (time > 0) {
					notify(s, (player, stack) -> {
						String timeStr = String.format("%d.%02d", time/20, (time%20)*5);

						BlockPos startPos = startPosMap.get(s);
						BlockPos endPos = pos;

						new Notice(startPos, NotificationComponentString.translated("notice.charset.stopwatch.timeStart", NotificationComponentString.raw(timeStr))).withStyle(NoticeStyle.DRAWFAR).sendTo(player);
						new Notice(endPos, NotificationComponentString.translated("notice.charset.stopwatch.timeEnd", NotificationComponentString.raw(timeStr))).withStyle(NoticeStyle.DRAWFAR).sendTo(player);
					});
				}
			}
		}

		for (String s : startPosListeners.get(pos)) {
			if (!startPosTimes.containsKey(s)) {
				startPosTimes.put(s, worldIn.getTotalWorldTime());

				/* notify(s, (player, stack) -> {
					stack.setItemDamage(1);
				}); */
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
	public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

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
