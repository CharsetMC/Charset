package pl.asie.charset.module.tools.wrench;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
        name = "quark:tools.wrench.rotate",
        profile = ModuleProfile.COMPAT,
        dependencies = {"tools.wrench", "mod:quark"}
)
public class WrenchCompatRotateQuark {
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Block verticalPlanks = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("quark:vertical_planks"));
        Block stainedPlanks = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("quark:stained_planks"));
        Block verticalStainedPlanks = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("quark:vertical_stained_planks"));

        if (verticalPlanks != null && verticalPlanks != Blocks.AIR) {
            CharsetToolsWrench.registerRotationHandler(Blocks.PLANKS, (world, pos, state, axis) -> world.setBlockState(pos, verticalPlanks.getStateFromMeta(state.getBlock().getMetaFromState(state))));
            CharsetToolsWrench.registerRotationHandler(verticalPlanks, (world, pos, state, axis) -> world.setBlockState(pos, Blocks.PLANKS.getStateFromMeta(state.getBlock().getMetaFromState(state))));
        }

        if (verticalStainedPlanks != null && verticalStainedPlanks != Blocks.AIR) {
            CharsetToolsWrench.registerRotationHandler(stainedPlanks, (world, pos, state, axis) -> world.setBlockState(pos, verticalStainedPlanks.getStateFromMeta(state.getBlock().getMetaFromState(state))));
            CharsetToolsWrench.registerRotationHandler(verticalStainedPlanks, (world, pos, state, axis) -> world.setBlockState(pos, stainedPlanks.getStateFromMeta(state.getBlock().getMetaFromState(state))));
        }
    }
}
