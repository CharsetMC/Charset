package pl.asie.charset.decoration;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.decoration.poster.EntityPoster;
import pl.asie.charset.decoration.poster.RenderPoster;
import pl.asie.charset.decoration.scaffold.ModelScaffold;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.storage.barrel.BarrelModel;

public class ProxyClient extends ProxyCommon {
    public static final ModelScaffold scaffoldModel = new ModelScaffold();

    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityPoster.class, new IRenderFactory<EntityPoster>() {
            @Override
            public Render<? super EntityPoster> createRenderFor(RenderManager manager) {
                return new RenderPoster(manager);
            }
        });
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation("charsetdecoration:scaffold", "normal"), scaffoldModel);
        event.getModelRegistry().putObject(new ModelResourceLocation("charsetdecoration:scaffold", "inventory"), scaffoldModel);

        ModelScaffold.scaffoldModel = (IRetexturableModel) RenderUtils.getModel(new ResourceLocation("charsetdecoration:block/scaffold"));
    }
}
