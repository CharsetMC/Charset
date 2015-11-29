package pl.asie.charset.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ProxyClient extends ProxyCommon {
	@Override
	public void registerItemModels() {
		ModelLoader.setCustomModelResourceLocation(ModCharsetLib.charsetIconItem, 0,
				new ModelResourceLocation("charsetlib:icon", "inventory"));
	}

	@Override
	public World getLocalWorld(int dim) {
	if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
		World w = Minecraft.getMinecraft().theWorld;
		if (w != null && w.provider.getDimensionId() == dim) {
			return w;
		} else {
			return null;
		}
	} else {
		return DimensionManager.getWorld(dim);
	}
}
}
