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
 */

package pl.asie.charset.tweaks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.tweaks.carry.CarryHandler;
import pl.asie.charset.tweaks.carry.CarryTransformerRegistry;
import pl.asie.charset.tweaks.carry.ICarryTransformer;
import pl.asie.charset.tweaks.carry.TweakCarry;

public class ProxyCommon {
	public void initMinecartTweakClient() {

	}

	public void initShardsTweakClient() {

	}

	public void carryGrabBlock(EntityPlayer player, World world, BlockPos pos) {
		CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
		if (carryHandler != null && !carryHandler.isCarrying()) {
			carryHandler.grab(world, pos);
		}
	}

	public void carryGrabEntity(EntityPlayer player, World world, Entity entity) {
		CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
		if (carryHandler != null && !carryHandler.isCarrying()) {
			for (ICarryTransformer<Entity> transformer : CarryTransformerRegistry.INSTANCE.getEntityTransformers()) {
				if (transformer.extract(entity, true) != null) {
					Pair<IBlockState, TileEntity> pair = transformer.extract(entity, false);
					carryHandler.put(pair.getLeft(), pair.getRight());
					return;
				}
			}
		}
	}
}
