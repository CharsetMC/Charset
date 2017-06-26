package pl.asie.charset.lib.wires;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public final class WireManager {
    public static final int MAX_ID = 255;
    public static final ForgeRegistry<WireProvider> REGISTRY = (ForgeRegistry<WireProvider>) new RegistryBuilder<WireProvider>()
            .setName(new ResourceLocation("charset:wire"))
            .setIDRange(1, MAX_ID)
            .setType(WireProvider.class)
            .create();

    private WireManager() {

    }
}
