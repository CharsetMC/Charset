package pl.asie.charset.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.DimensionManager;

public class ProxyClient extends ProxyCommon {
	@Override
	public World getLocalWorld(int dim) {
	if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
		World w = Minecraft.getMinecraft().theWorld;
		if (w != null && w.provider.dimensionId == dim) {
			return w;
		} else {
			return null;
		}
	} else {
		return DimensionManager.getWorld(dim);
	}
}
}
