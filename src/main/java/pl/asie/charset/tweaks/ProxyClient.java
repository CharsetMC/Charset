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

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.tweaks.carry.PacketCarryGrab;
import pl.asie.charset.tweaks.minecart.ModelMinecartWrapped;
import pl.asie.charset.tweaks.shard.ItemShard;
import pl.asie.charset.tweaks.shard.TweakGlassShards;

public class ProxyClient extends ProxyCommon {
	@Override
	public void initShardsTweakClient() {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemShard.Color(), TweakGlassShards.shardItem);
	}

	@Override
	public void initMinecartTweakClient() {
		Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Minecraft.getMinecraft().getRenderManager().entityRenderMap;

		for (Render<? extends Entity> e : entityRenderMap.values()) {
			if (e instanceof RenderMinecart) {
				Field f;

				try {
					f = RenderMinecart.class.getDeclaredField("modelMinecart");
				} catch (NoSuchFieldException eee) {
					try {
						f = RenderMinecart.class.getDeclaredField("field_77013_a");
					} catch (NoSuchFieldException ee) {
						f = null;
					}
				}

				if (f != null) {
					try {
						f.setAccessible(true);
						f.set(e, new ModelMinecartWrapped((ModelBase) f.get(e)));
					} catch (IllegalAccessException eee) {
						eee.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void carryGrabBlock(EntityPlayer player, World world, BlockPos pos) {
		if (!(player instanceof EntityPlayerMP)) {
			ModCharsetTweaks.packet.sendToServer(new PacketCarryGrab(world, pos));
		}
		super.carryGrabBlock(player, world, pos);
	}

	@Override
	public void carryGrabEntity(EntityPlayer player, World world, Entity entity) {
		if (!(player instanceof EntityPlayerMP)) {
			ModCharsetTweaks.packet.sendToServer(new PacketCarryGrab(world, entity));
		}
		super.carryGrabEntity(player, world, entity);
	}
}
