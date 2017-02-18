package pl.asie.charset.lib.wires;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.*;

public final class WireManager {
    public static final IForgeRegistry<WireFactory> REGISTRY = new RegistryBuilder()
            .setName(new ResourceLocation("charset:wire"))
            .setIDRange(1, 255)
            .setType(WireFactory.class)
            .create();

    public static ItemWire ITEM;

    private WireManager() {

    }

    public static void register(WireFactory factory) {
        REGISTRY.register(factory);
        // TODO
        // MultipartRegistry.registerPartFactory(factory, factory.getRegistryName().toString());
    }
}
