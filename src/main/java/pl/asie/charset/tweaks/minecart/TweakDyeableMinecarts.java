package pl.asie.charset.tweaks.minecart;

import net.minecraft.entity.item.EntityMinecart;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.tweaks.ModCharsetTweaks;
import pl.asie.charset.tweaks.Tweak;

public class TweakDyeableMinecarts extends Tweak {
	public TweakDyeableMinecarts() {
		super("additions", "dyeableMinecarts", "Dye minecarts by right-clicking them!", true);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public void enable() {
		ModCharsetTweaks.proxy.initMinecraftTweakClient();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event) {
		if (event.entity instanceof EntityMinecart) {
			event.entity.registerExtendedProperties(MinecartProperties.NAME, new MinecartProperties());
		}
	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.entity.worldObj.isRemote && event.entity instanceof EntityMinecart) {
			PacketMinecartRequest.send((EntityMinecart) event.entity);
		}
	}

	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event) {
		if (!event.target.worldObj.isRemote
				&& event.target instanceof EntityMinecart
				&& ColorUtils.isDye(event.entityPlayer.getHeldItem())) {
			MinecartProperties properties = MinecartProperties.get((EntityMinecart) event.target);
			if (properties != null) {
				properties.setColor(ColorUtils.getRGBColor(ColorUtils.getColorIDFromDye(event.entityPlayer.getHeldItem())));

				event.setCanceled(true);
				event.entityPlayer.swingItem();

				PacketMinecartUpdate.send((EntityMinecart) event.target);
			}
		}
	}
}
