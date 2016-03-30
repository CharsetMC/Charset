package pl.asie.charset.tweaks;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.ModCharsetLib;

public class TweakMobControl extends Tweak {
	private Set<Class<? extends Entity>> disabledClasses = new HashSet<Class<? extends Entity>>();
	private Configuration config;

	public TweakMobControl() {
		super("tweaks", "mobControl", "Control mob spawning. Upon enabling, refer to 'tweaks-mobcontrol.cfg'.", false);
	}

	private void reloadConfig() {
		config = new Configuration(ModCharsetLib.instance.getConfigFile("tweaks-mobcontrol.cfg"));

		for (String s : EntityList.getEntityNameList()) {
			Class<? extends Entity> entity = EntityList.stringToClassMapping.get(s);
			if (entity != null && EntityLiving.class.isAssignableFrom(entity)) {
				boolean enabled = config.get("allow", s, true, null).getBoolean();
				if (!enabled) {
					disabledClasses.add(entity);
				}
			}
		}

		config.save();
	}

	@Override
	public void enable() {
		reloadConfig();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void checkSpawn(LivingSpawnEvent.CheckSpawn event) {
		if (disabledClasses.contains(event.getEntity().getClass())) {
			event.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public void checkJoinWorld(EntityJoinWorldEvent event) {
		if (disabledClasses.contains(event.getEntity().getClass())) {
			event.setCanceled(true);
		}
	}
}
