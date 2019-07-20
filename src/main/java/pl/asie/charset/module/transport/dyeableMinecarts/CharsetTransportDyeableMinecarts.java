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

package pl.asie.charset.module.transport.dyeableMinecarts;

import akka.util.Reflect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.render.sprite.PixelOperationSprite;
import pl.asie.charset.lib.render.sprite.TextureWhitener;
import pl.asie.charset.lib.resources.CharsetFakeResourcePack;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

@CharsetModule(
		name = "transport.dyeableMinecarts",
		description = "Use dyes on Minecarts to make them colorful",
		profile = ModuleProfile.STABLE
)
public class CharsetTransportDyeableMinecarts {
	public static class CapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
		private final MinecartDyeable dyeable = new MinecartDyeable();

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

	@CharsetModule.PacketRegistry("dyeMinecarts")
	public static PacketRegistry packet;

	@CapabilityInject(MinecartDyeable.class)
	public static Capability<MinecartDyeable> MINECART_DYEABLE;
	public static ResourceLocation MINECART_DYEABLE_KEY = new ResourceLocation("charsettweaks:minecart_dyeable");

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		CharsetFakeResourcePack.INSTANCE.registerEntry(ModelMinecartWrapped.DYEABLE_MINECART, (stream) -> {
			try {
				BufferedImage image = RenderUtils.getTextureImage(ModelMinecartWrapped.MINECART, ModelLoader.defaultTextureGetter());
				int[] pixels = new int[image.getWidth() * image.getHeight()];
				image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
				TextureWhitener.INSTANCE.remap(
						pixels, image.getWidth(), ModelLoader.defaultTextureGetter(),
						ModelMinecartWrapped.MINECART, -1, (x,y) -> x < 44f/64f && y < 28f/32f, 8f/9f, false
				);
				image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

				ImageIO.write(image, "png", stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketMinecartUpdate.class);
		packet.registerPacket(0x02, PacketMinecartRequest.class);

		CapabilityManager.INSTANCE.register(MinecartDyeable.class, new MinecartDyeable.Storage(), MinecartDyeable::new);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void overrideRenderers(FMLPostInitializationEvent event) {
		Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Minecraft.getMinecraft().getRenderManager().entityRenderMap;

		try {
			Field f = ObfuscationReflectionHelper.findField(RenderMinecart.class, "field_77013_a");
			for (Render<? extends Entity> e : entityRenderMap.values()) {
				if (e instanceof RenderMinecart) {
					f.set(e, new ModelMinecartWrapped((ModelBase) f.get(e)));
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ReflectionHelper.UnableToFindFieldException e) {
			// pass
		}
	}

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent event) {
		if (event.getObject() instanceof EntityMinecart) {
			event.addCapability(MINECART_DYEABLE_KEY, new CapabilityProvider());
		}
	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getEntity().world.isRemote && event.getEntity() instanceof EntityMinecart) {
			PacketMinecartRequest.send((EntityMinecart) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		 if (event.getTarget() instanceof EntityMinecart
				 && !event.getTarget().getEntityWorld().isRemote
				 && event.getEntityPlayer().isSneaking()
				 && ColorUtils.isDye(event.getEntityPlayer().getHeldItem(event.getHand()))) {
			MinecartDyeable properties = MinecartDyeable.get((EntityMinecart) event.getTarget());
			if (properties != null) {
				properties.setColor(ColorUtils.getDyeColor(event.getEntityPlayer().getHeldItem(event.getHand())));

				event.setCanceled(true);
				event.getEntityPlayer().swingArm(event.getHand());

				PacketMinecartUpdate.send((EntityMinecart) event.getTarget());
			}
		}
	}
}
