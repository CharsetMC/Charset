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

package pl.asie.charset.tweaks.minecart;

import net.minecraft.entity.item.EntityMinecart;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.tweaks.ModCharsetTweaks;
import pl.asie.charset.tweaks.Tweak;

public class TweakDyeableMinecarts extends Tweak {
	public static class CapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
		private final IMinecartDyeable dyeable = new IMinecartDyeable.Impl();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == MINECART_DYEABLE;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == MINECART_DYEABLE ? (T) dyeable : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) MINECART_DYEABLE.writeNBT(dyeable, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			MINECART_DYEABLE.readNBT(dyeable, null, nbt);
		}
	}

	@CapabilityInject(IMinecartDyeable.class)
	public static Capability<IMinecartDyeable> MINECART_DYEABLE;
	public static ResourceLocation MINECART_DYEABLE_KEY = new ResourceLocation("charsettweaks:minecart_dyeable");

	public TweakDyeableMinecarts() {
		super("additions", "dyeableMinecarts", "Dye minecarts by right-clicking them!", true);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public void enable() {
		CapabilityManager.INSTANCE.register(IMinecartDyeable.class, new Capability.IStorage<IMinecartDyeable>() {
			@Override
			public NBTBase writeNBT(Capability<IMinecartDyeable> capability, IMinecartDyeable instance, EnumFacing side) {
				if (instance != null) {
					NBTTagCompound compound = new NBTTagCompound();
					compound.setInteger("color", instance.getColor());
					return compound;
				} else {
					return null;
				}
			}

			@Override
			public void readNBT(Capability<IMinecartDyeable> capability, IMinecartDyeable instance, EnumFacing side, NBTBase nbt) {
				if (nbt instanceof NBTTagCompound && instance != null) {
					NBTTagCompound compound = (NBTTagCompound) nbt;
					if (compound.hasKey("color")) {
						instance.setColor(compound.getInteger("color"));
					}
				}
			}
		}, IMinecartDyeable.Impl.class);
		ModCharsetTweaks.proxy.initMinecartTweakClient();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent event) {
		if (event.getObject() instanceof EntityMinecart) {
			event.addCapability(MINECART_DYEABLE_KEY, new CapabilityProvider());
		}
	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getEntity().worldObj.isRemote && event.getEntity() instanceof EntityMinecart) {
			PacketMinecartRequest.send((EntityMinecart) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		 if (!event.getTarget().worldObj.isRemote
				&& event.getTarget() instanceof EntityMinecart
				&& ColorUtils.isDye(event.getEntityPlayer().getHeldItem(event.getHand()))) {
			IMinecartDyeable properties = IMinecartDyeable.get((EntityMinecart) event.getTarget());
			if (properties != null) {
				properties.setColor(ColorUtils.getRGBColor(ColorUtils.getColorIDFromDye(event.getEntityPlayer().getHeldItem(event.getHand()))));

				event.setCanceled(true);
				event.getEntityPlayer().swingArm(event.getHand());

				PacketMinecartUpdate.send((EntityMinecart) event.getTarget());
			}
		}
	}
}
