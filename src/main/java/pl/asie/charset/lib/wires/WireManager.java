package pl.asie.charset.lib.wires;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.*;

public final class WireManager {
    public static final IForgeRegistry<WireProvider> REGISTRY = new RegistryBuilder()
            .setName(new ResourceLocation("charset:wire"))
            .setIDRange(1, 255)
            .setType(WireProvider.class)
            .create();

    public static ItemWire ITEM;

    private WireManager() {

    }

    public static void register(WireProvider factory) {
        REGISTRY.register(factory);
    }
}
