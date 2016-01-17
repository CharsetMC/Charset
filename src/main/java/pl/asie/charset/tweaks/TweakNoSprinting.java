package pl.asie.charset.tweaks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TweakNoSprinting extends Tweak {
	public TweakNoSprinting() {
		super("tweaks", "noPlayerSprinting", "Remove sprinting.", false);
	}

	@Override
	public void enable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.player.isSprinting()) {
			event.player.setSprinting(false);
		}
	}
}
