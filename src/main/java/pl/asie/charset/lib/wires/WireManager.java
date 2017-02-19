package pl.asie.charset.lib.wires;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.*;

public final class WireManager {
    public static final int MAX_ID = 255;
    public static final FMLControlledNamespacedRegistry<WireProvider> REGISTRY = (FMLControlledNamespacedRegistry<WireProvider>) new RegistryBuilder<WireProvider>()
            .setName(new ResourceLocation("charset:wire"))
            .setIDRange(1, MAX_ID)
            .setType(WireProvider.class)
            .create();

    @Deprecated
    public static ItemWire ITEM;

    private WireManager() {

    }

    public static void register(WireProvider factory) {
        REGISTRY.register(factory);
    }
}
