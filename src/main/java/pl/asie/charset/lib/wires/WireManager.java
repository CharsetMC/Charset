package pl.asie.charset.lib.wires;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public final class WireManager {
    public static final int MAX_ID = 255;
    public static ForgeRegistry<WireProvider> REGISTRY;

    private WireManager() {

    }
}
