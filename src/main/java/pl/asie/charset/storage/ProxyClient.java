package pl.asie.charset.storage;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.utils.ClientUtils;

/**
 * Created by asie on 1/10/16.
 */
public class ProxyClient extends ProxyCommon {
    public static IBakedModel[] backpackTopModel = new IBakedModel[4];

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileBackpack.class, new TileBackpackRenderer());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        IModel backpackTopModelBase = ClientUtils.getModel(new ResourceLocation("charsetstorage:block/backpack_top"));
        for (int i = 0; i < 4; i++) {
            backpackTopModel[i] = backpackTopModelBase.bake(
                    new TRSRTransformation(EnumFacing.getFront(i + 2)),
                    DefaultVertexFormats.BLOCK, ClientUtils.textureGetter);
        }
    }
}
