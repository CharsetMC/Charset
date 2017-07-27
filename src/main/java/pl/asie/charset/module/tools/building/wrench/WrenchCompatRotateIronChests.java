package pl.asie.charset.module.tools.building.wrench;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

@CharsetModule(
        name = "ironchest:tools.wrench.rotate",
        profile = ModuleProfile.COMPAT,
        dependencies = {"tools.wrench", "mod:ironchest"}
)
public class WrenchCompatRotateIronChests {
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Block ironChest = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("ironchest:iron_chest"));
        if (ironChest != null && ironChest != Blocks.AIR) {
            CharsetToolsBuilding.registerRotationHandler(ironChest, ((world, pos, state, axis) -> state.getBlock().rotateBlock(world, pos, EnumFacing.UP)));
        }
    }
}
