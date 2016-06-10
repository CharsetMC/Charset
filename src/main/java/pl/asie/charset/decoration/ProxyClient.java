package pl.asie.charset.decoration;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import pl.asie.charset.decoration.poster.EntityPoster;
import pl.asie.charset.decoration.poster.RenderPoster;

public class ProxyClient extends ProxyCommon {
    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityPoster.class, new IRenderFactory<EntityPoster>() {
            @Override
            public Render<? super EntityPoster> createRenderFor(RenderManager manager) {
                return new RenderPoster(manager);
            }
        });
    }
}
