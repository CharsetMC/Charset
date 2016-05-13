package pl.asie.charset.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.audio.manager.AudioStreamManagerClient;
import pl.asie.charset.lib.render.ModelFactory;

public class ProxyClient extends ProxyCommon {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		ModelFactory.clearCaches();
	}

	@Override
	public void init() {
		AudioStreamManager.INSTANCE = new AudioStreamManagerClient();
	}

	@Override
	public void registerItemModel(Item item, int meta, String name) {
		if (name.contains("#")) {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name.split("#")[0], name.split("#")[1]));
		} else {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"));
		}
	}

	@Override
	public World getLocalWorld(int dim) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			World w = Minecraft.getMinecraft().theWorld;
			if (w != null && w.provider.getDimension() == dim) {
				return w;
			} else {
				return null;
			}
		} else {
			return DimensionManager.getWorld(dim);
		}
	}

	@Override
	public void onServerStop() {
		AudioStreamManagerClient.INSTANCE.removeAll();
	}

	@Override
	public boolean isClientThread() {
		return Minecraft.getMinecraft().isCallingFromMinecraftThread();
	}

	@Override
	public void addScheduledClientTask(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	@Override
	public boolean isClient() {
		return true;
	}
}
