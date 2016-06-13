package pl.asie.charset.lib.wires;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.PersistentRegistryManager;

public final class WireManager {
    public static final FMLControlledNamespacedRegistry<WireFactory> REGISTRY = PersistentRegistryManager.createRegistry(
            new ResourceLocation("charsetlib:wires"), WireFactory.class, null, 1, 255, true,
            null, null, null
    );
    public static ItemWire ITEM;

    private WireManager() {

    }

    public static void register(WireFactory factory) {
        REGISTRY.register(factory);
        MultipartRegistry.registerPartFactory(factory, factory.getRegistryName().toString());
    }
}
