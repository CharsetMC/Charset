package pl.asie.charset.module.tweaks.fix;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.network.PacketRegistry;

import java.util.ArrayDeque;
import java.util.Queue;

@CharsetModule(
		name = "tweak.fixplayerdamageanimation",
		description = "Fixes player directional damage animation."
)
public class CharsetTweakFixPlayerDamageAnimation {
	@CharsetModule.PacketRegistry("fixPlyrDmgAnim")
	private PacketRegistry registry;
	private final Queue<EntityLivingBase> players = new ArrayDeque<>();

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		registry.registerPacket(0x01, PacketSyncAttackValue.class);
	}

	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayerMP && !event.getEntityLiving().getEntityWorld().isRemote) {
			players.add(event.getEntityLiving());
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER) {
			EntityLivingBase player;
			while (players.size() > 0) {
				player = players.remove();
				registry.sendTo(new PacketSyncAttackValue(player), (EntityPlayer) player);
			}
		}
	}
}
