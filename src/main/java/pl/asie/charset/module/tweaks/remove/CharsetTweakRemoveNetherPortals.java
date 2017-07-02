package pl.asie.charset.module.tweaks.remove;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
        name = "tweak.remove.netherPortals",
        profile = ModuleProfile.STABLE,
        isDefault = false
)
public class CharsetTweakRemoveNetherPortals {
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockPortalBlocked().setRegistryName("minecraft:portal"));
    }
}
