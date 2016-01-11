package pl.asie.charset.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ProxyClient extends ProxyCommon {
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
            if (w != null && w.provider.getDimensionId() == dim) {
                return w;
            } else {
                return null;
            }
        } else {
            return DimensionManager.getWorld(dim);
        }
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
