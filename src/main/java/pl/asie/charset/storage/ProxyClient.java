package pl.asie.charset.storage;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.utils.ClientUtils;

/**
 * Created by asie on 1/10/16.
 */
public class ProxyClient extends ProxyCommon {
    public static IModel backpackTopModel;

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileBackpack.class, new TileBackpackRenderer());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        backpackTopModel = ClientUtils.getModel(new ResourceLocation("charsetstorage:block/backpack_top"));
    }
}
